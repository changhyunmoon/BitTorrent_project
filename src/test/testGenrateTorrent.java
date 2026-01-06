package test;

import torrent.Bencoder;
import torrent.GenrateTorrent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class testGenrateTorrent {

    String torrentName = "mch_first_torrent";
    String trackerUrl = "www.trackerURL.com";
    String fileDirectory = "C:\\Users\\mch99\\OneDrive\\바탕 화면\\computer_network\\BitTorrent_project";
    String createdBy = "mch";
    String comment = "mch first .torrent file";
    int pieceLength = 100 * 1024;
    boolean isPrivate = true;
    List<String> fileNames = Arrays.asList("test1.txt", "test2.txt");
}
