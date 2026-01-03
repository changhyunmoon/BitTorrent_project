package torrent;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PieceHashGenerator {

    /**
     * 파일을 지정된 조각 크기로 읽어 전체 조각의 SHA-1 해시 바이트 배열을 생성합니다.
     * * @param file          해시를 계산할 대상 파일
     * @param pieceLength   조각당 크기 (예: 262144)
     * @return 모든 조각의 해시가 이어붙여진 바이트 배열 (각 조각당 20바이트)
     */
    public static byte[] generatePiecesHash(File file, int pieceLength) throws IOException, NoSuchAlgorithmException {
        // SHA-1 알고리즘 인스턴스 생성
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

        // 각 조각의 20바이트 해시값들을 모으기 위한 스트림
        ByteArrayOutputStream piecesBuffer = new ByteArrayOutputStream();

        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[pieceLength];
            int bytesRead;

            System.out.println("🧩 파일 해시 분석 중: " + file.getName());

            while ((bytesRead = is.read(buffer)) != -1) {
                // 읽어온 데이터만큼 SHA-1 업데이트
                sha1.update(buffer, 0, bytesRead);

                // 현재 조각의 해시 계산 (20바이트)
                byte[] hash = sha1.digest();

                // 결과 버퍼에 추가
                piecesBuffer.write(hash);
            }
        }

        return piecesBuffer.toByteArray();
    }
}