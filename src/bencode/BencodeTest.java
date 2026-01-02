package bencode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BencodeTest {

    private final String encodeFileName = "encode_test.torrent";
    private final String decodeFilename = "decode_test.txt";

    /**
     * [Step 1: 인코딩]
     * 사용자 입력을 받아 Bencode 파일을 생성합니다.
     */
    public void runBencodeTest(String userInput) {
        try {
            System.out.println("\n--- [Bencode 인코딩 테스트 시작] ---");
            Map<String, Object> originalData = createMockData(userInput);

            // Bencode 형식으로 바이너리 파일 저장
            saveToBencodeFile(originalData, encodeFileName);
            System.out.println("결과: '" + encodeFileName + "' 파일 생성 완료.");
        } catch (Exception e) {
            System.err.println("인코딩 중 오류: " + e.getMessage());
        }
    }

    /**
     * [Step 2: 디코딩 및 텍스트 저장]
     * Bencode 파일을 읽어 사람이 읽을 수 있는 텍스트(.txt)로 저장합니다.
     */
    public void runBdecodeTest() {
        try {
            System.out.println("\n--- [Bdecode 디코딩 테스트 시작] ---");

            // 1. 파일 읽기
            Map<String, Object> decodedData = loadFromFile(encodeFileName);

            if (decodedData != null) {
                // 2. 읽어온 데이터를 사람이 읽기 좋은 텍스트 파일로 저장
                saveToTextFile(decodedData, decodeFilename);
                System.out.println("Step 3: 읽어온 내용을 텍스트 형식으로 '" + decodeFilename + "'에 저장했습니다.");

                // 3. 콘솔에도 출력
                System.out.println("===== [디코딩 내용 출력] =====");
                System.out.println(formatData(decodedData));
            }
        } catch (Exception e) {
            System.err.println("디코딩 테스트 중 오류 발생!");
            e.printStackTrace();
        }
    }

    /**
     * 데이터를 텍스트(.txt) 파일로 저장하는 메서드
     */
    private void saveToTextFile(Map<String, Object> data, String fileName) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("===== Bdecode Result =====");
            writer.println(formatData(data));
            writer.println("==========================");
        }
    }

    /**
     * Map 데이터를 문자열 형식으로 보기 좋게 포맷팅
     */
    private String formatData(Object obj) {
        StringBuilder sb = new StringBuilder();
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            map.forEach((k, v) -> {
                sb.append(k).append(" : ").append(formatData(v)).append("\n");
            });
        } else if (obj instanceof byte[]) {
            // 바이트 배열(문자열) 처리
            sb.append(new String((byte[]) obj, StandardCharsets.UTF_8));
        } else if (obj instanceof List) {
            sb.append(obj.toString());
        } else {
            sb.append(obj.toString());
        }
        return sb.toString();
    }

    // Bencode 인코딩 저장 (바이너리)
    private void saveToBencodeFile(Map<String, Object> data, String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            Bencode.encode(data, fos);
        }
    }

    private Map<String, Object> loadFromFile(String fileName) throws IOException {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            Bdecode decoder = new Bdecode(fis);
            return (Map<String, Object>) decoder.decode();
        }
    }

    private Map<String, Object> createMockData(String userInput) {
        Map<String, Object> torrent = new TreeMap<>();
        torrent.put("announce", userInput);
        Map<String, Object> info = new TreeMap<>();
        info.put("name", "file_" + userInput + ".dat");
        info.put("piece length", 16384L);
        byte[] pieces = new byte[20];
        new Random().nextBytes(pieces);
        info.put("pieces", pieces);
        torrent.put("info", info);
        return torrent;
    }
}