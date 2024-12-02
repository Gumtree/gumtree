package au.gov.ansto.bragg.nbi.server.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.util.IO;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.gumtree.security.EncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import au.gov.ansto.bragg.nbi.server.NBIServerProperties;

public class GitService {

	private static Logger logger = LoggerFactory.getLogger(GitService.class);
	private static String keyPath;
	private static String passphrase;
	private Git git;
	private String repoPath;
	private String remoteAddress;
	private Repository repository;
	private SshSessionFactory sshSessionFactory;
	
	public GitService(String path) {
		repoPath = path;
		try {
			repository = new FileRepository(path + "/.git");
			git = new Git(repository);
		} catch (IOException e) {
			e.printStackTrace();
		}
		keyPath = System.getProperty(NBIServerProperties.PROPERTY_SSH_KEYPATH);
		try {
//			passphrase = EncryptionUtils.decryptBase64(System.getProperty(PROPERTY_SSH_PASSPHRASE));
			passphrase = EncryptionUtils.decryptProperty(NBIServerProperties.PROPERTY_SSH_PASSPHRASE);
		} catch (Exception e1) {
		}
		logger.error(keyPath);
		logger.error(passphrase);
		setCredential();
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
	
	public String getRemoteAddress() {
		return remoteAddress;
	}
	
	private void setCredential() {
		sshSessionFactory = new JschConfigSessionFactory() {
			
			@Override
			protected void configure(Host arg0, Session arg1) {
				
			}
			
			@Override
			protected JSch createDefaultJSch( FS fs ) throws JSchException {
			  com.jcraft.jsch.Logger schLogger = new com.jcraft.jsch.Logger() {
				
				@Override
				public void log(int level, String message) {
					switch (level) {
					case com.jcraft.jsch.Logger.DEBUG:
						logger.debug(message);
						break;
					case com.jcraft.jsch.Logger.INFO:
						logger.info(message);
						break;
					case com.jcraft.jsch.Logger.WARN:
						logger.warn(message);
						break;
					case com.jcraft.jsch.Logger.ERROR:
						logger.error(message);
						break;
					case com.jcraft.jsch.Logger.FATAL:
						logger.error(message);
						break;
					default:
						logger.debug(message);
						break;
					}
				}
				
				@Override
				public boolean isEnabled(int level) {
					return true;
				}
			};
			  JSch.setLogger(schLogger);
			  JSch defaultJSch = super.createDefaultJSch( fs );
//			  defaultJSch.addIdentity( "/path/to/private_key" );
			  defaultJSch.addIdentity(keyPath, passphrase);
//			  java.util.Properties config = new java.util.Properties(); 
//			  config.put("StrictHostKeyChecking", "no");
//			  defaultJSch.setConfig(config);
			  JSch.setConfig("StrictHostKeyChecking", "no");
			  return defaultJSch;
			}
		};
		
//		CredentialsProvider credential = new 
//		PushCommand push = git.push();
//		push.setCredentialsProvider(credentialsProvider)
	}
	
	public void applyChange() throws GitException {
		if (git != null) {

			AddCommand addCommand = git.add();
			try {
				Status status = git.status().call();

				Set<String> modified = status.getModified();
				Set<String> untracked = status.getUntracked();
				
				for (String filePattern : modified) {
					addCommand.addFilepattern(filePattern);					
				}
				for (String filePattern : untracked) {
					addCommand.addFilepattern(filePattern);					
				}
				addCommand.call();
				logger.info("apply change to repository: " + String.valueOf(repoPath));
			} catch (NoFilepatternException e) {
//				e.printStackTrace();
				throw new GitException("Git error: no change", e);
			} catch (GitAPIException e) {
				throw new GitException("Git error: failed to add.", e);
			}
		} else {
			throw new GitException("Git error: git repository not ready");
		}
	}
	
	public void commit(String message) throws GitException {
		if (git != null) {
			CommitCommand commit = git.commit();
			try {
				commit.setAuthor("gumtree","gumtree@ansto.gov.au");
				commit.setMessage(message).call();
				logger.info("commit change to repository: " + String.valueOf(repoPath) + " with message '" + message + "'");
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
			throw new GitException("Git error: git repository not ready");
		}
	}

	public void push() throws GitException {
		if (git != null) {
			PushCommand push = git.push();
			push.setTransportConfigCallback(new TransportConfigCallback() {
				
				@Override
				public void configure(Transport transport) {
					SshTransport sshTransport = ( SshTransport )transport;
				    sshTransport.setSshSessionFactory( sshSessionFactory );					
				}
			});
			
			try {
				push.setRemote(remoteAddress);
				Iterable<PushResult> results = push.call();
				logger.info("push repository: " + String.valueOf(repoPath) + " to remote git at: " + remoteAddress);
				for (PushResult result : results) {
					logger.info("Pushed " + result.getMessages() + " " + result.getURI() + " updates: " + String.valueOf(result.getRemoteUpdates()));
	            }
			} catch (InvalidRemoteException e) {
				throw new GitException("Git error: invalid remote repository.", e);
			} catch (TransportException e) {
				throw new GitException("Git error: network connection failed.", e);
			} catch (GitAPIException e) {
				throw new GitException("Git error: illegal API.", e);
			}
		} else {
			throw new GitException("Git error: git repository not ready");
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
			treewalk.setRecursive(false);
//			treewalk.setFilter(PathFilter.create(path));
			if (treewalk != null) {
				if (path.endsWith(treewalk.getPathString())) {
					byte[] data = reader.open(treewalk.getObjectId(0)).getBytes();
					return new String(data, "utf-8");
				}
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
	
	public List<String> getFileHistory(String path) {
		List<String> history = new ArrayList<String>();
		try {
			List<GitCommit> commits = getCommits(path);
			for (GitCommit commit : commits) {
				history.add(commit.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return history;
	}
	
	public List<GitCommit> getCommits(String path) throws RevisionSyntaxException, MissingObjectException, 
	IncorrectObjectTypeException, AmbiguousObjectException, IOException, NoHeadException, GitAPIException {
//		List<RevCommit> commits = new ArrayList<RevCommit>();
//
//		LogCommand logCommand = git.log()
//		        .add(git.getRepository().resolve(Constants.HEAD))
//		        .addPath(path);
//
//		for (RevCommit revCommit : logCommand.call()) {
//		    commits.add(revCommit);
//		}
		List<GitCommit> commits = new ArrayList<GitCommit>();

		RevWalk revWalk = new RevWalk(repository);
		revWalk.setTreeFilter(
		        AndTreeFilter.create(
		                PathFilterGroup.createFromStrings(path),
		                TreeFilter.ANY_DIFF)
		);

		RevCommit rootCommit = revWalk.parseCommit(repository.resolve(Constants.HEAD));
		revWalk.sort(RevSort.COMMIT_TIME_DESC);
		revWalk.markStart(rootCommit);

		for (RevCommit revCommit : revWalk) {
		    commits.add(new GitCommit(revCommit));
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
//					.setPathFilter(PathFilter.create("Page_2015-12-01T16-17-25.xml"))
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
		String oldHash = "3d8822fbb37dbc96df7ccc6d296ae4c966782850";

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
	
	/**
	 * Create a string to a UTF-8 temporary file and return the path.
	 *
	 * @param body
	 *            complete content to write to the file. If the file should end
	 *            with a trailing LF, the string should end with an LF.
	 * @return path of the temporary file created within the trash area.
	 * @throws IOException
	 *             the file could not be written.
	 */
	protected File write(final String body) throws IOException {
		final File f = File.createTempFile("temp", "txt");
		try {
			write(f, body);
			return f;
		} catch (Error e) {
			f.delete();
			throw e;
		} catch (RuntimeException e) {
			f.delete();
			throw e;
		} catch (IOException e) {
			f.delete();
			throw e;
		}
	}

	/**
	 * Write a string as a UTF-8 file.
	 *
	 * @param f
	 *            file to write the string to. Caller is responsible for making
	 *            sure it is in the trash directory or will otherwise be cleaned
	 *            up at the end of the test. If the parent directory does not
	 *            exist, the missing parent directories are automatically
	 *            created.
	 * @param body
	 *            content to write to the file.
	 * @throws IOException
	 *             the file could not be written.
	 */
	protected void write(final File f, final String body) throws IOException {
		writeFile(f, body);
	}

	public static void writeFile(final File f, final String body)
			throws IOException {
		FileUtils.mkdirs(f.getParentFile(), true);
		Writer w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
		try {
			w.write(body);
		} finally {
			w.close();
		}
	}
	
	public static String read(final File file) throws IOException {
		final byte[] body = IO.readFully(file);
		return new String(body, 0, body.length, "UTF-8");
	}
	
	public String testDiff() throws GitAPIException, NoWorkTreeException, IOException {
		String res = "";
//		Repository db = git.getRepository();
//		write(new File(db.getWorkTree(), "test.txt"), "test");
//		File folder = new File(db.getWorkTree(), "folder");
//		folder.mkdir();
//		write(new File(folder, "folder.txt"), "folder");
//		Git git = new Git(db);
//		git.add().addFilepattern(".").call();
//		git.commit().setMessage("Initial commit").call();
//		write(new File(folder, "folder.txt"), "folder change");
//		git.add().addFilepattern(".").call();
//		git.commit().setMessage("second commit").call();
//		write(new File(folder, "folder.txt"), "second folder change");
//		git.add().addFilepattern(".").call();
//		git.commit().setMessage("third commit").call();

		// bad filter
		DiffCommand diff = git.diff().setShowNameAndStatusOnly(true)
				.setPathFilter(PathFilter.create("test.txt"))
				.setOldTree(getTreeIterator("HEAD^^"))
				.setNewTree(getTreeIterator("HEAD^"));
		List<DiffEntry> entries = diff.call();
		res += "size == 0 ? " + entries.size() + ";\n";

		// no filter, two commits
		OutputStream out = new ByteArrayOutputStream();
		diff = git.diff().setOutputStream(out)
				.setOldTree(getTreeIterator("1411d94af36927206604e89e7353794694cec5b2^"))
				.setNewTree(getTreeIterator("4a64f0d30238bcc89ebedcf414e009d479218496^"));
		entries = diff.call();
		res += "size == 1 ? " + entries.size() + ";\n";
		res += "ChangeType == MODIFY ?" + entries.get(0).getChangeType() + ";\n";
		res += "old path == folder/folder.txt ? " + entries.get(0).getOldPath() + ";\n";
		res += "new path == folder/folder.txt ? " + entries.get(0).getNewPath() + ";\n";

		String actual = out.toString();
		String expected = "diff --git a/folder/folder.txt b/folder/folder.txt\n"
				+ "index 0119635..95c4c65 100644\n"
				+ "--- a/folder/folder.txt\n"
				+ "+++ b/folder/folder.txt\n"
				+ "@@ -1 +1 @@\n"
				+ "-folder\n"
				+ "\\ No newline at end of file\n"
				+ "+folder change\n"
				+ "\\ No newline at end of file\n";
		res += "is expected ? " + (expected.toString().equals(actual)) + ";\n";
		return res;
	}
	
	public String readRevisionOfFile(String path, String id) {
		ObjectId oid = ObjectId.fromString(id);
		
		try {
			RevWalk revWalk = new RevWalk(repository);
            RevCommit commit = revWalk.parseCommit(oid);
            // and using commit's tree find the path
            RevTree tree = commit.getTree();
            System.out.println("Having tree: " + tree);

            // now try to find a specific file
            try  {
            	TreeWalk treeWalk = new TreeWalk(repository);
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(path));
                if (!treeWalk.next()) {
                    throw new IllegalStateException("Did not find expected file 'README.md'");
                }

                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);

                // and then one can the loader to read the file
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                loader.copyTo(baos);
                return new String(baos.toByteArray(), Charset.defaultCharset());
            } catch (Exception e) {
            	e.printStackTrace();
            }
            revWalk.dispose();

		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	public class GitCommit {
		
		private String id;
		private String name;
		private String shortMessage;
		private String message;
		private int timestamp;
		
		public GitCommit(RevCommit revCommit) {
			id = revCommit.getId().toString();
			name = revCommit.getName();
			shortMessage = revCommit.getShortMessage();
			message = revCommit.getFullMessage();
			timestamp = revCommit.getCommitTime();
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getMessage() {
			return message;
		}

		public int getTimestamp() {
			return timestamp;
		}
		
		public String getShortMessage() {
			return shortMessage;
		}
	}
}
