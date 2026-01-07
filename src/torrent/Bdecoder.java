package torrent;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Bdecoder {

    private final byte[] data;
    private int index = 0;

    public Bdecoder(byte[] data) {
        this.data = data;
    }

    /**
     * 바이트 배열을 입력받아 전체를 디코딩하고 맵으로 반환합니다.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> decode(byte[] data) throws Exception {
        Bdecoder decoder = new Bdecoder(data);
        Object result = decoder.decodeObject();
        if (!(result instanceof Map)) {
            throw new Exception("Root element is not a dictionary");
        }
        return (Map<String, Object>) result;
    }

    private Object decodeObject() throws Exception {
        byte c = data[index++];
        if (c == 'i') {
            return decodeInteger();
        } else if (c == 'l') {
            return decodeList();
        } else if (c == 'd') {
            return decodeDictionary();
        } else if (Character.isDigit(c)) {
            index--; // 숫자는 길이를 읽기 위해 다시 뒤로 한 칸
            return decodeBytes();
        }
        throw new Exception("Invalid bencode type: " + (char) c);
    }

    // 정수 디코딩: i123e -> 123L
    private Long decodeInteger() throws Exception {
        int start = index;
        while (data[index] != 'e') {
            index++;
        }
        String val = new String(data, start, index - start, StandardCharsets.UTF_8);
        index++; // 'e' 건너뛰기
        return Long.parseLong(val);
    }

    // 바이트 배열 디코딩: 5:hello -> byte[]
    private byte[] decodeBytes() throws Exception {
        int start = index;
        while (data[index] != ':') {
            index++;
        }
        int length = Integer.parseInt(new String(data, start, index - start, StandardCharsets.UTF_8));
        index++; // ':' 건너뛰기

        byte[] result = new byte[length];
        System.arraycopy(data, index, result, 0, length);
        index += length;
        return result;
    }

    // 리스트 디코딩: l...e -> List
    private List<Object> decodeList() throws Exception {
        List<Object> list = new ArrayList<>();
        while (data[index] != 'e') {
            list.add(decodeObject());
        }
        index++; // 'e' 건너뛰기
        return list;
    }

    // 딕셔너리 디코딩: d...e -> TreeMap (키 정렬 보장)
    private Map<String, Object> decodeDictionary() throws Exception {
        Map<String, Object> map = new TreeMap<>();
        while (data[index] != 'e') {
            // 키는 무조건 byte string이므로 문자열로 변환
            String key = new String(decodeBytes(), StandardCharsets.UTF_8);
            Object value = decodeObject();
            map.put(key, value);
        }
        index++; // 'e' 건너뛰기
        return map;
    }
}
