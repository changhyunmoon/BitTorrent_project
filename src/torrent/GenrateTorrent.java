package torrent;


import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class GenrateTorrent {

    // 기본 조각 크기: 256KB (요구사항에 따라 가변적 설정 가능)
    private static final int DEFAULT_PIECE_LENGTH = 256 * 1024;

    /**
     * 단일 또는 다중 파일을 위한 토렌트 생성 메소드
     */
    public Map<String, Object> createTorrent(String trackerUrl, String directoryName, List<File> files, String createdBy, String comment)
            throws Exception {
        System.out.println("createTorrent start");
        System.out.println("params -> trackerUrl = " + trackerUrl + ", directoryName = " + directoryName + ", files = " + files + ", createdBy = " + createdBy + ", comment = " + comment);
        System.out.println("");
        Map<String, Object> torrent = new HashMap<>();

        // 1. 메타데이터 (Meta Data) 설정
        torrent.put("announce", trackerUrl);
        torrent.put("created by", createdBy);
        torrent.put("comment", comment);
        torrent.put("creation date", System.currentTimeMillis() / 1000); // Unix Timestamp

        // 2. Info 딕셔너리 구성
        Map<String, Object> info = new HashMap<>();
        info.put("name", directoryName);
        info.put("piece length", DEFAULT_PIECE_LENGTH);

        // 3. 파일 목록 처리 (Files Filename)
        List<Map<String, Object>> filesList = new ArrayList<>();
        for (File file : files) {
            Map<String, Object> fileMap = new HashMap<>();
            fileMap.put("length", file.length());
            // 비트토렌트 규격상 path는 리스트 형태여야 함
            fileMap.put("path", Collections.singletonList(file.getName()));
            filesList.add(fileMap);
        }
        info.put("files", filesList);

        // 4. 조각 해싱 (Piece Hashing) 실행
        // 모든 파일의 내용을 순서대로 이어 붙여서 조각낸 결과값을 가져옴
        byte[] pieces = calculatePiecesHash(files, DEFAULT_PIECE_LENGTH);
        info.put("pieces", pieces);

        torrent.put("info", info);

        return torrent;

        // 5. Bencoding 실행 (최종 바이트 배열 반환)
        //return Bencoder.encode(torrent);
    }

    /**
     * 모든 파일을 하나의 가상 데이터 흐름으로 보고 조각별 SHA-1 해시를 계산
     */
    private byte[] calculatePiecesHash(List<File> files, int pieceLength)
            throws IOException, NoSuchAlgorithmException {

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        ByteArrayOutputStream piecesStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[pieceLength];
        int bufferPos = 0;

        for (File file : files) {
            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                int bytesRead;
                while ((bytesRead = is.read(buffer, bufferPos, pieceLength - bufferPos)) != -1) {
                    bufferPos += bytesRead;

                    // 버퍼가 꽉 찼을 때 (한 조각이 완성되었을 때) 해싱
                    if (bufferPos == pieceLength) {
                        sha1.update(buffer, 0, pieceLength);
                        piecesStream.write(sha1.digest());
                        bufferPos = 0; // 버퍼 초기화
                    }
                }
            }
        }

        // 마지막 남은 자투리 데이터가 있다면 해싱 (Last Piece)
        if (bufferPos > 0) {
            sha1.update(buffer, 0, bufferPos);
            piecesStream.write(sha1.digest());
        }

        return piecesStream.toByteArray();
    }
}