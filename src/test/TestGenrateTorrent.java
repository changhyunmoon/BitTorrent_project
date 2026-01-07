package test;

import torrent.TorrentGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestGenrateTorrent {

    public void runTestGenerateTorrent(){

        String rootFolder = "testFolder";
        String trackerUrl = "www.trackerURL.com";
        List<String> announceList = null;
        String createdBy = "mch";
        String comment = "mch first .torrent file";
        int pieceLength = 100 * 1024;
        boolean isPrivate = true;
        List<String> fileNames = Arrays.asList("test1.txt", "test2.txt", "test3.txt");
        String encoding = "UTF-8";

        try{
            TorrentGenerator gt = new TorrentGenerator();
            Map<String, Object> torrent = gt.generateMultiFileTorrent(
                    rootFolder, trackerUrl, announceList,
                    fileNames, createdBy, comment, pieceLength, isPrivate, encoding);
            System.out.println("\ntest torrent 파일 생성 완료\n");

            //torrent 결과 출력
            gt.printTorrent(torrent);

        }catch(Exception e){
            System.err.println("토렌트 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }

    }

}
