package torrent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Bencoder {

    /*
    ByteArrayOutputStream는 자바에서 데이터를 메모리에 임시로 저장하기 위한 도구
    보통 데이터를 파일이나 네트워크로 보내기 전에 조각조각 나눠진 데이터들을 하나로 뭉쳐야 할 때 유용하게 사용한다.
    자동 크기 조절: 데이터를 넣을 때마다 내부 배열 크기가 자동으로 늘어난다
    메모리 기반 : 하드디스크나 네트워크가 아닌 RAM에서 작업하기 때문에 속도가 매우 빠르다
    데이터 변환 : 쌓인 데이터를 한꺼번에 bytes[] 배열로 바꾸거나 문자열로 변환하기 쉽다.
     */
    //외부용 인터페이스
    public static byte[] encode(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        encode(obj, out);
        return out.toByteArray();
    }

    //내부 로직용 encode
    private static void encode(Object obj, ByteArrayOutputStream out) throws IOException {
        //instanceof 객체의 클래스 타입 확인
        if (obj instanceof String) {
            String s = (String) obj;
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            out.write(String.valueOf(bytes.length).getBytes());
            out.write(':');
            out.write(bytes);
        } else if (obj instanceof byte[]) { // pieces 해시 처리를 위함
            byte[] bytes = (byte[]) obj;
            out.write(String.valueOf(bytes.length).getBytes());
            out.write(':');
            out.write(bytes);
        } else if (obj instanceof Integer || obj instanceof Long) {
            out.write('i');
            out.write(obj.toString().getBytes());
            out.write('e');
        } else if (obj instanceof List) {
            out.write('l');
            for (Object item : (List<?>) obj) {
                encode(item, out);
            }
            out.write('e');
        } else if (obj instanceof Map) {
            out.write('d');
            // Bencode 규칙: 딕셔너리의 키는 반드시 사전순 정렬되어야 함
            Map<String, Object> map = (Map<String, Object>) obj;
            List<String> keys = new ArrayList<>(map.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                encode(key, out);       // 키 인코딩
                encode(map.get(key), out); // 값 인코딩
            }
            out.write('e');
        }
    }
}

