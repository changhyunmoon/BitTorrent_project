package test;

import torrent.TorrentGenerator;
import tracker.TrackerSeedingRequest;

import java.util.Arrays;
import java.util.Map;

public class TestSeeding {

    public void runTestSeeding() throws Exception{

        //torrent 생성
        TorrentGenerator torrentGenerator = new TorrentGenerator();
        Map<String, Object> torrent = torrentGenerator.generateMultiFileTorrent("./testFolder",
                "http://tracker.open/announce", null,
                Arrays.asList("test1.txt"), "Me",
                "Test", 262144, false, "UTF-8");

        //info hash 계산
        byte[] bencodedTorrent = torrentGenerator.bencodeTorrent(torrent);
        byte[] infoHash = torrentGenerator.calInfoHash(bencodedTorrent);

        //트래커에게 알리기
        TrackerSeedingRequest seeder = new TrackerSeedingRequest();
        Map<String, Object> response = seeder.announce(
                (String) torrent.get("announce"),
                infoHash,
                6881, //내 리슨 포트
                0,
                0,
                0,              //시더이므로 남은 용량 0
                "started"
        );

        System.out.println("트래커 응답 : " + response);
    }

}
