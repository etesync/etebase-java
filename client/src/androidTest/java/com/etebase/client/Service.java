package com.etebase.client;

import androidx.test.runner.AndroidJUnit4;

import com.etebase.client.exceptions.Base64Exception;
import com.etebase.client.exceptions.UnauthorizedException;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

import okhttp3.OkHttpClient;

import static java.nio.charset.StandardCharsets.UTF_8;

@RunWith(AndroidJUnit4.class)
public class Service {
    private static String storedSession = "gqd2ZXJzaW9uAa1lbmNyeXB0ZWREYXRhxQGr_KWyDChQ6tXOJwJKf0Kw3QyR99itPIF3vZ5w6pVXSIq7AWul3fIXjIZOsBEwTVRumw7e9Af38D5oIL2VLNPLlmTOMjzIvuB00z3zDMFbH8pwrg2p_FvAhLHGjUGoXzU2XIxS4If7rQUfEz1zWkHPqWMrj4hACML5fks302dOUw7OsSMekcQaaVqMyj82MY3lG2qj8CL6ykSED7nW6OYWwMBJ1rSDGXhQRd5JuCGl6kgAHxKS6gkkIAWeUKjC6-Th2etk1XPKDiks0SZrQpmuXG8h_TBdd4igjRUqnIk09z5wvJFViXIU4M3pQomyFPk3Slh7KHvWhzxG0zbC2kUngQZ5h-LbVTLuT_TQWjYmHiOIihenrzl7z9MLebUq6vuwusZMRJ1Atau0Y2HcOzulYt4tLRP49d56qFEId3R4xomZ666hy-EFodsbzpxEKHeBUro3_gifOOKR8zkyLKTRz1UipZfKvnWk_RHFgZlSClRsXyaP34wstUavSiz-HNmTEmflNQKM7Awfel108FcSbW9NQAogW2Y2copP-P-R-DiHThrXmgDsWkTQFA";
    private static String SERVER_URL = "http://10.100.102.8:8033";
    private static OkHttpClient httpClient =  new OkHttpClient.Builder()
            // don't allow redirects by default, because it would break PROPFIND handling
            .followRedirects(false)
            .build();
    private static String COL_TYPE = "some.coltype";

    @Test
    public void testSmoketest() {
        Client client = Client.create(httpClient, SERVER_URL);
        Account etebase = Account.restore(client, storedSession, null);
        etebase.forceServerUrl(SERVER_URL);
        etebase.fetchToken();
        CollectionManager col_mgr = etebase.getCollectionManager();
        ItemMetadata collectionMetadata = new ItemMetadata();
        Collection col = col_mgr.create(COL_TYPE, collectionMetadata, "Something".getBytes());
        byte[] content = col.getContent();
        String str = new String(content, UTF_8);
        assertEquals(str, "Something");
        FetchOptions fetchOptions = new FetchOptions().prefetch(PrefetchOption.Auto);
        col_mgr.upload(col, fetchOptions);
        CollectionListResponse col_list = col_mgr.list(COL_TYPE, null);
        assertNotEquals(col_list.getData().length, 0);
        fetchOptions = new FetchOptions().stoken(col_list.getStoken());
        col_list = col_mgr.list(COL_TYPE, fetchOptions);
        assertEquals(col_list.getData().length, 0);

        Collection col2 = col_mgr.fetch(col.getUid(), null);
        byte[] content2 = col2.getContent();
        String str2 = new String(content2, UTF_8);
        assertEquals(str2, "Something");
        col2.setContent("Something else".getBytes());
        col_mgr.transaction(col2, null);

        ItemManager it_mgr = col_mgr.getItemManager(col);
        ItemMetadata itemMetadata = new ItemMetadata();
        itemMetadata.setItemType("Bla");
        Item item = it_mgr.create(itemMetadata, "Something item".getBytes());
        assertNotEquals(item.getUid(), "");
        assertNotNull(item.getEtag());
        byte[] it_content = item.getContent();
        String it_str = new String(it_content, UTF_8);
        assertEquals(it_str, "Something item");
        Item[] emptyArray = new Item[] {};
        it_mgr.batch(new Item[] {item}, null, null);
        assertNotNull(item.getEtag());
        item.setContent("Something item2".getBytes());
        it_mgr.transaction(new Item[] {item}, emptyArray, null);
        ItemListResponse item_list = it_mgr.list(null);
        assertEquals(item_list.getData().length, 1);
        Item it2_first = item_list.getData()[0];
        fetchOptions = new FetchOptions().stoken(item_list.getStoken());
        item_list = it_mgr.list(fetchOptions);
        assertEquals(item_list.getData().length, 0);
        assertEquals(new String(it2_first.getContent(), UTF_8), "Something item2");

        etebase.logout();
    }

    @Test
    public void testRemovedCollections() {
        Client client = Client.create(httpClient, SERVER_URL);
        Account etebase = Account.restore(client, storedSession, null);
        etebase.forceServerUrl(SERVER_URL);
        etebase.fetchToken();
        CollectionManager col_mgr = etebase.getCollectionManager();
        ItemMetadata collectionMetadata = new ItemMetadata();
        Collection col = col_mgr.create(COL_TYPE, collectionMetadata, "Something".getBytes());
        col_mgr.upload(col, null);
        CollectionListResponse col_list = col_mgr.list(COL_TYPE);
        FetchOptions fetchOptions = new FetchOptions().stoken(col_list.getStoken());
        col_list = col_mgr.list(COL_TYPE, fetchOptions);
        assertEquals(0, col_list.getRemovedMemberships().length);

        // FIXME: actually test we get the removed memberships ever.

        etebase.logout();
    }

    @Test
    public void testCache() {
        Client client = Client.create(httpClient, SERVER_URL);
        Account etebase = Account.restore(client, storedSession, null);
        etebase.forceServerUrl(SERVER_URL);
        etebase.fetchToken();
        CollectionManager col_mgr = etebase.getCollectionManager();
        ItemMetadata collectionMetadata = new ItemMetadata();
        Collection col = col_mgr.create(COL_TYPE, collectionMetadata, "Something".getBytes());
        col_mgr.upload(col,null);

        {
            CollectionListResponse collections = col_mgr.list(COL_TYPE, null);
            int length = collections.getData().length;
            assert(length > 0);
            collections = col_mgr.list(new String[] {"bad1", COL_TYPE, "bad2"}, null);
            assertEquals(length, collections.getData().length);
            collections = col_mgr.list(new String[] {"bad1", "bad2"});
            assertEquals(0, collections.getData().length);
        }

        String cached = etebase.save(null);
        etebase = Account.restore(client, cached,null);
        col_mgr = etebase.getCollectionManager();
        col = col_mgr.fetch(col.getUid(),null);
        assertArrayEquals(col.getContent(), "Something".getBytes());
        byte[] cachedCol = col_mgr.cacheSave(col);
        col = col_mgr.cacheLoad(cachedCol);
        assertEquals(col.getCollectionType(), COL_TYPE);

        ItemManager it_mgr = col_mgr.getItemManager(col);
        ItemMetadata itemMetadata = new ItemMetadata();
        itemMetadata.setItemType("Bla");
        Item item = it_mgr.create(itemMetadata, "Something item".getBytes());
        it_mgr.batch(new Item[] {item}, null, null);
        byte[] cachedItem = it_mgr.cacheSaveWithContent(item);
        item = it_mgr.cacheLoad(cachedItem);
        assertArrayEquals(item.getContent(), "Something item".getBytes());

        etebase.logout();
    }

    @Test
    public void testBase64() {
        String encoded = Utils.toBase64("Test".getBytes());
        byte[] decoded = Utils.fromBase64(encoded);
        assertArrayEquals(decoded, "Test".getBytes());
    }

    @Test
    public void testStringContent() throws UnsupportedEncodingException {
        Client client = Client.create(httpClient, SERVER_URL);
        Account etebase = Account.restore(client, storedSession, null);

        CollectionManager colMgr = etebase.getCollectionManager();
        ItemMetadata collectionMetadata = new ItemMetadata();
        Collection col = colMgr.create(COL_TYPE, collectionMetadata, "Something");
        assertEquals(col.getContentString(), "Something");
        col.setContent("Another");
        assertEquals(col.getContentString(), "Another");
    }

    @Test(expected=Base64Exception.class)
    public void base64Exception() {
        Utils.fromBase64("#@$@#$*@#$");
    }

    @Test(expected= UnauthorizedException.class)
    public void testUanuthorizedException() {
        Client client = Client.create(httpClient, SERVER_URL);
        Account etebase = Account.restore(client, storedSession, null);
        etebase.forceServerUrl(SERVER_URL);
        etebase.fetchToken();
        etebase.logout();
        CollectionManager collectionManager = etebase.getCollectionManager();
        collectionManager.list(COL_TYPE, null);
    }
}
