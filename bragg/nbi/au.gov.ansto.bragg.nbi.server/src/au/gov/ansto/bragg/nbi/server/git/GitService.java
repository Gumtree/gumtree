package au.gov.ansto.bragg.nbi.server.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

public class GitService {

	private Git git;
	
	public GitService(String path) {
		Repository repository;
		try {
			repository = new FileRepository(path + "/.git");
			git = new Git(repository);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void commit(String message) throws GitException {
		if (git != null) {
			CommitCommand commit = git.commit();
			try {
				commit.setMessage(message).call();
			} catch (NoHeadException e) {
				throw new GitException("Git error: no head found.", e);
			} catch (NoMessageException e) {
				throw new GitException("Git error: no message.", e);
			} catch (UnmergedPathsException e) {
				throw new GitException("Git error: can not merge.", e);
			} catch (ConcurrentRefUpdateException e) {
				throw new GitException("Git error: concurrent ref update.", e);
			} catch (WrongRepositoryStateException e) {
				throw new GitException("Git error: wrong repository state.", e);
			} catch (GitAPIException e) {
				throw new GitException("Git error: illegal API.", e);
			}
		} else {
			throw new GitException("git repository not ready");
		}
	}

	public String fetchBlob(String revSpec, String path) throws MissingObjectException, IncorrectObjectTypeException,
	IOException {
		Repository repo = git.getRepository();
		// Resolve the revision specification
		final ObjectId id = repo.resolve(revSpec);

		// Makes it simpler to release the allocated resources in one go
		ObjectReader reader = repo.newObjectReader();
		RevWalk walk = null;
		try {
			// Get the commit object for that revision
			walk = new RevWalk(reader);
			RevCommit commit = walk.parseCommit(id);

			// Get the revision's file tree
			RevTree tree = commit.getTree();
			// .. and narrow it down to the single file's path
//			TreeWalk treewalk = TreeWalk.forPath(reader, path, tree);

			TreeWalk treewalk = new TreeWalk(reader);
			treewalk.addTree(repo.resolve(revSpec + "^{tree}"));
			treewalk.setRecursive(true);
			treewalk.setFilter(PathFilter.create(path));
			if (treewalk != null) {
				while (treewalk.next()) {
					if (path.endsWith(treewalk.getPathString())) {
						byte[] data = reader.open(treewalk.getObjectId(0)).getBytes();
						return new String(data, "utf-8");
					}
				}
				// use the blob id to read the file's data
			} else {
				return "";
			}
		} finally {
			reader.release();
			if (walk != null) {
				walk.dispose();
			}
		}
		return "";
	}
	
	public List<String> findCommits(String path) {
		List<String> commits = new ArrayList<String>();
		String head = Constants.HEAD;
		Repository repository = git.getRepository();
		RevWalk rw = new RevWalk(repository);
		try {
			ObjectId oldId = repository.resolve(head);
			ObjectId oldTree = repository.resolve("HEAD^{tree}");
			rw.markStart(rw.parseCommit(oldId));
			commits.add(oldId.getName());
			
			Iterator<RevCommit> it = rw.iterator();
			ObjectReader reader = git.getRepository().newObjectReader();
			while(it.hasNext()) {
				CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
				CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
				oldTreeIter.reset( reader, oldTree );
				RevCommit commit = it.next();
				System.err.println(commit.getName());
				ObjectId newId = commit.getId();
//				RevTree newTree = commit.getTree();
				ObjectId newTree = repository.resolve("HEAD~1^{tree}");
				newTreeIter.reset( reader, newTree );
				DiffFormatter diffFormatter = new DiffFormatter( DisabledOutputStream.INSTANCE );
				diffFormatter.setRepository( git.getRepository() );
				List<DiffEntry> entries = diffFormatter.scan( oldTreeIter, newTreeIter );
				for (DiffEntry entry : entries) {
					if (path.equals(entry.getNewPath())){
						commits.add(newId.getName());
						oldId = newId;
						oldTree = newTree;
					}
				}
				
//			  RevTree tree = commit.getTree();
//			  TreeWalk treewalk = TreeWalk.forPath(repository, path, tree);
////			  treewalk.setFilter(TreeFilter.ANY_DIFF);
//			  if (treewalk != null ) {
////				  while (treewalk.next()) {
//					if (path.equals(treewalk.getPathString())){
//						commits.add(treewalk.getObjectId(0).getName());
//					}
//					treewalk.release();
////				}
////				  commits.add(treewalk.getNameString());
////				  int commitTime = commit.getCommitTime();
////				  commits.add(String.valueOf(commit.getId().getName()));
//			  }
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			rw.dispose();			
		}
		return commits;
	}
	
	private List<DiffEntry> getDiffEntries(String rev1, String rev2) 
			throws RevisionSyntaxException, AmbiguousObjectException, 
			IncorrectObjectTypeException, IOException {
		ObjectReader reader = git.getRepository().newObjectReader();
		CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
		ObjectId oldTree = git.getRepository().resolve( rev1 );
		oldTreeIter.reset( reader, oldTree );
		CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
		ObjectId newTree = git.getRepository().resolve( rev2 );
		newTreeIter.reset( reader, newTree );

		DiffFormatter diffFormatter = new DiffFormatter( DisabledOutputStream.INSTANCE );
		diffFormatter.setRepository( git.getRepository() );
		List<DiffEntry> entries = diffFormatter.scan( oldTreeIter, newTreeIter );

//		for( DiffEntry entry : entries ) {
//		  System.out.println( entry.getChangeType() );
//		}
		return entries;
	}
	
	public String compareCommits(){
		String ret = "";
		try {
			Repository repo = git.getRepository();
//			ObjectId head = repo.resolve("HEAD^{tree}");
//			ObjectId previousHead = repo.resolve("HEAD~^{tree}");
//			// Instanciate a reader to read the data from the Git database
//			ObjectReader reader = repo.newObjectReader();
//			// Create the tree iterator for each commit
//			CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
//			oldTreeIter.reset(reader, previousHead);
//			CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
//			newTreeIter.reset(reader, head);
//			List<DiffEntry> listDiffs = git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();
//			// Simply display the diff between the two commits
//			for (DiffEntry diff : listDiffs) {
//			        System.err.println(diff);
//			}
			DiffCommand diff = git.diff().setShowNameAndStatusOnly(true)
					.setPathFilter(PathFilter.create("Page_2015-12-01T16-17-25.xml"))
					.setOldTree(getTreeIterator("HEAD^^"))
					.setNewTree(getTreeIterator("HEAD^"));
			List<DiffEntry> entries = diff.call();
			for (DiffEntry entry : entries) {
				ret += entry;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	private AbstractTreeIterator getTreeIterator(String name)
			throws IOException {
		final ObjectId id = git.getRepository().resolve(name);
		if (id == null)
			throw new IllegalArgumentException(name);
		final CanonicalTreeParser p = new CanonicalTreeParser();
		try {
			ObjectReader or = git.getRepository().newObjectReader();
			p.reset(or, new RevWalk(git.getRepository()).parseTree(id));
			return p;
		} catch (Exception e) {
			// TODO: handle exception
			throw new IOException("can not find " + name);
		}
	}
	
	public String getDiff() {
		String ret = "";
		String oldHash = "05b36deb504b7c72c086448ff6fd7ba1cc91a12d";

		try {
			ObjectId headId = git.getRepository().resolve("HEAD^{tree}");
			ObjectId oldId = git.getRepository().resolve(oldHash + "^{tree}");

			ObjectReader reader = git.getRepository().newObjectReader();

			CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
			oldTreeIter.reset(reader, oldId);
			CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
			newTreeIter.reset(reader, headId);

			List<DiffEntry> diffs= git.diff()
					.setNewTree(newTreeIter)
					.setOldTree(oldTreeIter)
					.call();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DiffFormatter df = new DiffFormatter(out);
			df.setRepository(git.getRepository());

			for(DiffEntry diff : diffs)
			{
				df.format(diff);
				diff.getOldId();
				String diffText = out.toString("UTF-8");
				ret += diffText;
				out.reset();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public List<String> listCommits(String path) {
		List<String> commits = new ArrayList<String>();
//		String head = Constants.HEAD;
//		Repository repository = git.getRepository();
//		RevWalk rw = new RevWalk(repository);
//		try {
//			final ObjectId headId = repository.resolve(head);
//			//			rw.markStart(rw.parseCommit(headId));
//			RevCommit commit = rw.parseCommit(headId);
//			RevTree tree = commit.getTree();
//			TreeWalk treewalk = new TreeWalk(repository);
//			treewalk.addTree(tree);
//			for (RevCommit parent : commit.getParents()){
//				treewalk.addTree(parent.getTree());
//			}
//			treewalk.setFilter(TreeFilter.ANY_DIFF);
//
//			while (treewalk.next()){
//				if (treewalk.isSubtree() && path == treewalk.getPathString()){
//					commits.add(treewalk.getObjectId(0).getName());
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			rw.dispose();			
//		}
		Repository repository = git.getRepository();
		TreeWalk tw = new TreeWalk(repository);
		RevWalk rw = new RevWalk(repository);
		try {
			ObjectId headId = repository.resolve(Constants.HEAD);
			RevCommit commit = rw.parseCommit(headId);
			RevTree tree = commit.getTree();
			tw.addTree(tree); // tree ¡®0¡¯
			tw.setRecursive(true);
			tw.setFilter(PathFilter.create(path));
			if (tw.next()) {
				commits.add(commit.getName());
			}
//			while(tw.next()) {
//			  ObjectId id = tw.getObjectId(0);
////			  repository.open(id).copyTo(System.out);
//			  if (path.equals(tw.getPathString())){
//				  commits.add(String.valueOf(id));
//			  }
////			  commits.add(tw.getPathString());
//			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			tw.release();
		}
		return commits;
	}
}
