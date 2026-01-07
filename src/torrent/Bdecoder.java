package torrent;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class Bdecoder {

    private final byte[] data;
    private int index = 0;

    public Bdecoder(byte[] data) {
        this.data = data;
    }

    public static Map<String, Object> decode(byte[] data) throws Exception {
        Bdecoder decoder = new Bdecoder(data);
        return (Map<String, Object>) decoder.decodeObject();
    }

    private Object decodeObject() throws Exception {
        if (index >= data.length) return null;

        byte c = data[index];
        // 중요: 여기서 index++를 하지 않습니다. 각 메서드가 시작 문자를 직접 확인합니다.
        if (c == 'i') {
            return decodeInteger();
        } else if (c == 'l') {
            return decodeList();
        } else if (c == 'd') {
            return decodeDictionary();
        } else if (Character.isDigit(c)) {
            return decodeBytes();
        }
        throw new Exception("지원하지 않는 타입: " + (char) c + " (index: " + index + ")");
    }

    private Long decodeInteger() throws Exception {
        index++; // 'i' 건너뛰기
        int start = index;
        while (index < data.length && data[index] != 'e') {
            index++;
        }
        String val = new String(data, start, index - start, StandardCharsets.UTF_8);
        index++; // 'e' 건너뛰기
        return Long.parseLong(val);
    }

    private byte[] decodeBytes() throws Exception {
        int start = index;
        while (index < data.length && data[index] != ':') {
            index++;
        }
        // 숫자 부분만 추출 (예: "8:encoding"에서 "8"만)
        String lenStr = new String(data, start, index - start, StandardCharsets.UTF_8);
        int length = Integer.parseInt(lenStr);

        index++; // ':' 건너뛰기

        byte[] result = new byte[length];
        System.arraycopy(data, index, result, 0, length);
        index += length; // 데이터 길이만큼 정확히 이동
        return result;
    }

    private List<Object> decodeList() throws Exception {
        index++; // 'l' 건너뛰기
        List<Object> list = new ArrayList<>();
        while (index < data.length && data[index] != 'e') {
            list.add(decodeObject());
        }
        index++; // 리스트 끝 'e' 건너뛰기
        return list;
    }

    private Map<String, Object> decodeDictionary() throws Exception {
        index++; // 'd' 건너뛰기
        Map<String, Object> map = new TreeMap<>();
        while (index < data.length && data[index] != 'e') {
            // 딕셔너리의 키는 항상 문자열(byte[])입니다.
            byte[] keyBytes = decodeBytes();
            String key = new String(keyBytes, StandardCharsets.UTF_8);

            // 값 디코딩
            Object value = decodeObject();
            map.put(key, value);
        }
        index++; // 딕셔너리 끝 'e' 건너뛰기
        return map;
    }
}