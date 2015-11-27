package au.gov.ansto.bragg.nbi.server.git;

import java.io.IOException;

import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;

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
	
}
