package test;

import torrent.GenrateTorrent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestGenrateTorrent {

    public void runTestGenerateTorrent(){

        String torrentName = "mch_first_torrent";
        String trackerUrl = "www.trackerURL.com";
        String fileDirectory = "C:\\Users\\mch99\\OneDrive\\바탕 화면\\computer_network\\BitTorrent_project";
        String createdBy = "mch";
        String comment = "mch first .torrent file";
        int pieceLength = 100 * 1024;
        boolean isPrivate = true;
        List<String> fileNames = Arrays.asList("test1.txt", "test2.txt");

        try{
            GenrateTorrent gt = new GenrateTorrent();
            Map<String, Object> torrent = gt.createTorrent(
                    torrentName, trackerUrl, fileDirectory, createdBy,
                    comment, pieceLength, isPrivate, fileNames);
            System.out.println("\ntest torrent 파일 생성 완료\n");

            //torrent 결과 출력
            gt.showTorrent(torrent);

        }catch(Exception e){
            System.err.println("토렌트 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }

    }

}
