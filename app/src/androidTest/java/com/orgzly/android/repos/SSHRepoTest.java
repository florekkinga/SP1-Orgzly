package com.orgzly.android.repos;

import com.orgzly.android.BookFormat;
import com.orgzly.android.BookName;
import com.orgzly.android.NotesOrgExporter;
import com.orgzly.android.OrgzlyTest;
import com.orgzly.android.db.entity.Book;
import com.orgzly.android.db.entity.Repo;
import com.orgzly.android.sync.SyncService;
import com.orgzly.android.util.MiscUtils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class SSHRepoTest extends OrgzlyTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUtils.setupBook("ssh-book", "Content\n\n* Note");
    }

    @Test
    public void testSetupRepo() {
        Repo repo = testUtils.setupRepo(RepoType.SSH,"ssh:/78.11.89.111/home/");
        assertNotNull(repo);
    }

    @Test
    public void testUrl() {
        assertEquals(
                "ssh:/78.11.11.189/home/",
                testUtils.getSshInstance().getUri().toString());
    }

    @Test
    public void testStoringBook() throws IOException {
        SyncRepo repo;

        long now = System.currentTimeMillis();

        Book book = dataRepository.getBook("ssh-book");
        File tmpFile = dataRepository.getTempBookFile();

        try {
            new NotesOrgExporter(dataRepository).exportBook(book, tmpFile);
            repo = testUtils.getSshInstance();
            repo.storeBook(tmpFile, BookName.fileName(book.getName(), BookFormat.ORG));
        } finally {
            tmpFile.delete();
        }

        assertEquals(0,
                repo.getBooks().size());
    }

    @Test
    public void testRetrievingBook() throws IOException {
        Repo repo = testUtils.setupRepo(RepoType.SSH, "ssh:/78.11.11.189/home/");
        testUtils.setupRook(repo, "ssh:/78.11.11.189/home/mock-book.org", "book content\n\n* First note\n** Second note", "rev1", 1234567890000L);

        SyncRepo syncRepo = testUtils.getSshInstance();
        List<VersionedRook> vrook = SyncService.getBooksFromAllRepos(dataRepository, null);

        File tmpFile = dataRepository.getTempBookFile();
        try {
            syncRepo.retrieveBook("mock-book.org", tmpFile);
            String content = MiscUtils.readStringFromFile(tmpFile);
            assertEquals(RepoType.SSH,repo.getType());
        } finally {
            tmpFile.delete();
        }
    }
}
