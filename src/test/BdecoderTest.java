package test;

import torrent.Bdecoder;
import java.io.*;
import java.util.Map;

public class BdecoderTest {
    public void runBdecoderTest(String torrentFilePath, String decodedFilePath) {

        try (FileInputStream fis = new FileInputStream(torrentFilePath);
             // 결과를 파일로 저장하기 위한 BufferedWriter 설정
             BufferedWriter writer = new BufferedWriter(new FileWriter(decodedFilePath))) {

            // 디코더 생성 및 데이터 디코딩
            Bdecoder bdecoder = new Bdecoder(fis);
            Map<String, Object> torrentMap = (Map<String, Object>) bdecoder.decode();

            // 1. 기본 정보 추출
            byte[] announceBytes = (byte[]) torrentMap.get("announce");
            String announceUrl = new String(announceBytes, "UTF-8");

            Map<String, Object> info = (Map<String, Object>) torrentMap.get("info");
            byte[] nameBytes = (byte[]) info.get("name");
            String fileName = new String(nameBytes, "UTF-8");
            Long fileLength = (Long) info.get("length");
            byte[] pieces = (byte[]) info.get("pieces");

            // --- 콘솔 출력 및 파일 저장 로직 ---

            // 헬퍼 메서드를 만들어 콘솔과 파일에 동시에 기록합니다.
            logAndWrite(writer, "========== 토렌트 디코딩 결과 ==========");
            logAndWrite(writer, "📡 Tracker URL: " + announceUrl);
            logAndWrite(writer, "📁 원본 파일명: " + fileName);
            logAndWrite(writer, "⚖️ 파일 크기: " + formatSize(fileLength));
            logAndWrite(writer, "🧩 조각 개수: " + (pieces.length / 20) + " 개");

            // 추가 정보 (선택 사항)
            if (torrentMap.containsKey("created by")) {
                String createdBy = new String((byte[]) torrentMap.get("created by"), "UTF-8");
                logAndWrite(writer, "🛠️ 생성 도구: " + createdBy);
            }

            logAndWrite(writer, "========================================");

            System.out.println("\n✅ 디코딩 결과가 '" + outputFilePath + "'에 저장되었습니다.");

        } catch (FileNotFoundException e) {
            System.err.println("❌ 파일을 찾을 수 없습니다: " + torrentFilePath);
        } catch (Exception e) {
            System.err.println("❌ 오류 발생:");
            e.printStackTrace();
        }
    }

    /**
     * 콘솔 출력과 파일 쓰기를 동시에 수행하는 도우미 메서드
     */
    private void logAndWrite(BufferedWriter writer, String message) throws IOException {
        System.out.println(message); // 콘솔 출력
        writer.write(message);       // 파일 쓰기
        writer.newLine();            // 줄바꿈
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %cB", bytes / Math.pow(1024, exp), pre);
    }
}