package test;

import torrent.Bencoder;
import torrent.InfoHashCalculator;
import torrent.PieceHashGenerator; // 새로 만든 클래스 임포트

import java.io.*;
import java.util.*;

public class BencoderTest {

    public void runBencoderTest(String announce, String filePath, String encodedFilePath) {
        File sourceFile = new File(filePath);
        if (!sourceFile.exists()) {
            System.out.println("no file : " + filePath);
            return;
        }

        try {
            // 1. PieceHashGenerator를 사용하여 조각 해시 생성
            int pieceLength = 262144;
            byte[] pieces = PieceHashGenerator.generatePiecesHash(sourceFile, pieceLength);

            // 2. info 맵 구성
            Map<String, Object> info = new HashMap<>();
            info.put("name", sourceFile.getName());
            info.put("piece length", (long) pieceLength);
            info.put("length", sourceFile.length());
            info.put("pieces", pieces);

            // 3. InfoHashCalculator를 사용하여 info_hash 계산
            byte[] infoHash = InfoHashCalculator.calculateInfoHash(info);
            System.out.println("🆔 Info Hash: " + InfoHashCalculator.bytesToHex(infoHash));

            // 4. 전체 데이터 구성 및 저장 (이전과 동일)
            Map<String, Object> torrentData = new HashMap<>();
            torrentData.put("announce", announce);
            torrentData.put("info", info);

            try (FileOutputStream fos = new FileOutputStream(encodedFilePath + ".torrent")) {
                Bencoder.encode(torrentData, fos);
                System.out.println("✅ 토렌트 파일 저장 완료!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}