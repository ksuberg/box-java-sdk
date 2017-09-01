package com.box.sdk.example;


import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxCollaboration;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxUser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * To run via command line: gradle runScaleTestForTos
 */
public final class ScaleTestForTos {
    private static final String DEVELOPER_TOKEN = "QXh4B1w8O0ViJMZVhI8t5zaYsj00eoPF";

    private static final String CLIENT_ID =  "e0tvvrr5jpe8h8b52m9tgwa0ut77f0bo";
    private static final String CLIENT_SECRET = "mmPOBl2DOXMuIPRVqNuLVR1WRVOLnxE7";
    private static final String AUTH_CODE = "Uc9j800x4YrmMPXPZokdxQvaPiabmIGG";

    private static final int NUM_ITEMS = 2000;

    private static final int MAX_CACHE_ENTRIES = 100;
    private static final int MAX_DEPTH = 1;
    private static final String TEST_FOLDER_NAME = "ScaleTestTos";
//    private static final String COLLABORATOR_ID = "2400258453";
//    private static final String COLLABORATOR_EMAIL = "kberger+liveexternaltest3@box.com";
    private static final String COLLABORATOR_ID = "2389369966";
    private static final String COLLABORATOR_EMAIL = "kberger+liveexternaltest2@box.com";
//    private static final String COLLABORATOR_ID = "1809438003";
//    private static final String COLLABORATOR_EMAIL = "kberger+liveexternaltest1@box.com";
    private static final String COLLABORATOR_DEVELOPER_TOKEN = "rnQoLrEs3CrQ1iw4mE9IL83k442Ta60D";
    public static final String TEST_FILE_NAME = "TestFile";
    public static final String TEST_SUBFOLDER_NAME = "Test_Subfolder";
    public static final String TEST_FILE_CONTENT = "some content";

    private ScaleTestForTos() { }

    public static void main(String[] args) throws IOException {
        // Turn off logging to prevent polluting the output.
        Logger.getLogger("com.box.sdk").setLevel(Level.WARNING);
        System.out.println("Args" + args);


        //It is a best practice to use an access token cache to prevent unneeded requests to Box for access tokens.
        //For production applications it is recommended to use a distributed cache like Memcached or Redis, and to
        //implement IAccessTokenCache to store and retrieve access tokens appropriately for your environment.
//        IAccessTokenCache accessTokenCache = new InMemoryLRUAccessTokenCache(MAX_CACHE_ENTRIES);
//        Reader reader = new FileReader("src/example/config/config.json");
//        BoxConfig boxConfig = BoxConfig.readFrom(reader);
//        BoxDeveloperEditionAPIConnection api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig, accessTokenCache);

//        BoxAPIConnection api = new BoxAPIConnection(DEVELOPER_TOKEN);
        BoxAPIConnection api = new BoxAPIConnection(CLIENT_ID, CLIENT_SECRET, AUTH_CODE);

        BoxUser.Info userInfo = BoxUser.getCurrentUser(api).getInfo();
        System.out.format("Welcome, %s <%s>!\n\n", userInfo.getName(), userInfo.getLogin());

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);

        //// Get/create test folder
        BoxItem.Info testFolderInfo = getChildByName(TEST_FOLDER_NAME, rootFolder);
        if (testFolderInfo == null) {
//       //     testFolderInfo = rootFolder.createFolder(TEST_FOLDER_NAME);
            throw new RuntimeException("No test folder found " + TEST_FOLDER_NAME);
        }
        BoxFolder testFolder = (BoxFolder) testFolderInfo.getResource();

        //// Phase #1: Create collaborated folders

//        createTestFilesAndSendCollaborations(testFolder);

        //      //  createTestFiles(testFolder);
        //
        //      //  BoxCollaboration.sendCollaborations(api, testFolder, COLLABORATOR_EMAIL, BoxCollaboration.Role.VIEWER); //tranquility jobs, get request ID and check splunk

        //        listFolder(rootFolder, 0);

        //// Phase #2: delete collaborated folders
        //    //    deleteFolder(testFolder);

        deleteFilesInFolder(testFolder);

        //        listFolder(rootFolder, 0);
    }

    private static void deleteFolder(BoxFolder testFolder) {
        System.out.println("Delete test folder...");
        testFolder.delete(true);
    }

    private static void deleteFilesInFolder(BoxFolder testFolder) {
        System.out.println("Delete test folders... ");
        int numDeleted = 0;
        for (BoxItem.Info itemInfo : testFolder) {
            if (itemInfo instanceof BoxFolder.Info) {
                BoxFolder item = (BoxFolder) itemInfo.getResource();
                item.delete(false);
            } else if (itemInfo instanceof BoxFile.Info) {
                BoxFile item = (BoxFile) itemInfo.getResource();
                item.delete();
            } else {
                System.out.println("Weird item "+itemInfo.getID() + " name "+itemInfo.getName());
            }
            ++numDeleted;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Deleted " + numDeleted + " files/folders.");
    }

    private static void createTestFilesAndSendCollaborations(BoxFolder testFolder) {
        System.out.println("Create test folders and send collaborations...");

        for (int i = 0; i < NUM_ITEMS; i++) {
            BoxFolder.Info testSubFolder = testFolder.createFolder(TEST_SUBFOLDER_NAME + i);
            testSubFolder.getResource().collaborate(COLLABORATOR_EMAIL, BoxCollaboration.Role.VIEWER, false, false);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
//        uploadFileToFolder(testFolder, TEST_FILE_NAME + i + ".txt", TEST_FILE_CONTENT);
        }
        System.out.println("Created " + NUM_ITEMS + " collaborations.");
    }

    private static void createTestFiles(BoxFolder testFolder) {
        System.out.println("Create test folders...");

        for (int i = 0; i < NUM_ITEMS; i++) {
            BoxFolder.Info testSubFolder = testFolder.createFolder(TEST_SUBFOLDER_NAME + i);
//            System.out.println("   Created folder "+ testSubFolder.getResource().getID());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
//        uploadFileToFolder(testFolder, TEST_FILE_NAME + i + ".txt", TEST_FILE_CONTENT);
        }
    }

    private static void uploadFileToFolder(BoxFolder testFolder, String fileName, String contents) {
        InputStream inputStream = new ByteArrayInputStream(contents.getBytes());
        testFolder.uploadFile(inputStream, fileName);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BoxItem.Info getChildByName(String name, BoxFolder parentFolder) {
        for (BoxItem.Info child : parentFolder) {
            if (child.getName().equals(name)) {
                return child;
            }
            if (child instanceof BoxFolder.Info) {
                getChildByName(name, (BoxFolder) child.getResource());
            }
        }

        return null;
    }

    private static void listFolder(BoxFolder folder, int depth) {
        for (BoxItem.Info itemInfo : folder) {
            String indent = "";
            for (int i = 0; i < depth; i++) {
                indent += "    ";
            }

            System.out.println(indent + itemInfo.getName());
            if (itemInfo instanceof BoxFolder.Info) {
                BoxFolder childFolder = (BoxFolder) itemInfo.getResource();
                if (depth < MAX_DEPTH) {
                    listFolder(childFolder, depth + 1);
                }
            }
        }
    }
}
