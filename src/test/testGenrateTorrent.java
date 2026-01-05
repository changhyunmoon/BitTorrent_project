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
import java.util.Map;

public class testGenrateTorrent {

    public static void runTestGenrateTorret(){
        System.out.println("GenrateTorret Test Start");
        try{
            GenrateTorrent genrateTorrent = new GenrateTorrent();
            Map<String, Object> res = new HashMap<>();

            File file = new File("test.txt");

            res = genrateTorrent.createTorrent(
                    "http://tracker.example.com/announce",
                    "test_directory",
                    Arrays.asList(file),
                    "mch",
                    "first torrent file create"
            );

            System.out.println("generate torrent file res : " + res);
            //생성된 torrent encoding
            byte[] bres = Bencoder.encode(res);

            //encoding된 파일 저장
            String btorrent_path = "test_BencodingTorrent.torrent";
            try(FileOutputStream fos1 = new FileOutputStream(btorrent_path)){
                fos1.write(bres);

            }
            System.out.println("💾 바이너리 파일 저장 완료: " + btorrent_path);

            // 3. 가독성용 텍스트 파일 저장 (.txt)
            String torrent_path = "test_Torrent.txt";
            try (PrintWriter writer = new PrintWriter(new FileWriter(torrent_path))) {
                writer.println("======= TORRENT FILE STRUCTURE =======");
                // Map의 내용을 들여쓰기와 함께 예쁘게 출력
                formatToText(res, writer, 0);
                writer.println("======================================");
            }
            System.out.println("📄 가독성용 텍스트 파일 저장 완료: " + torrent_path);

        }catch (Exception e){
            System.err.println("오류 발생 : " + e.getMessage());
            e.printStackTrace();
        }
    }
    // 사람이 읽기 좋은 형태로 Map을 출력해주는 헬퍼 메소드
    private static void formatToText(Object obj, PrintWriter writer, int indent) {
        String space = "  ".repeat(indent);

        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            map.forEach((k, v) -> {
                writer.print(space + k + ": ");
                if (v instanceof Map || v instanceof java.util.List) {
                    writer.println();
                    formatToText(v, writer, indent + 1);
                } else {
                    formatToText(v, writer, 0); // 값 출력
                }
            });
        } else if (obj instanceof java.util.List) {
            for (Object item : (java.util.List<?>) obj) {
                formatToText(item, writer, indent + 1);
            }
        } else if (obj instanceof byte[]) {
            // 바이너리를 ISO_8859_1로 변환하면 깨진 문자 형태로라도 모든 바이트가 출력됩니다.
            String rawString = new String((byte[]) obj, StandardCharsets.ISO_8859_1);
            writer.println(rawString);
        } else {
            writer.println(obj);
        }
    }
}
