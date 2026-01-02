package bencode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

//Bdecoder 코드는 스트림에서 바이트를 하나씩 읽으며 재귀적으로 구조를 파악한다.
public class Bdecode {
    /*
    일반적인 스트림은 앞으로만 읽지만, 이 클래스는 읽었던 바이트를 다시 스트림으로 밀어넣는 기능이 있다.
    이게 숫자인가? 하고 읽어봤다가 아니면 다시 돌려놓기 위함이다.
     */
    private final PushbackInputStream in;

    public Bdecode(InputStream is) {
        this.in = new PushbackInputStream(is);
    }

    //데이터가 어떤 타입인지 판단하는 컨트롤 타워
    public Object decode() throws IOException {
        int c = in.read(); //현재 스트림에서 첫 번째 바이트를 읽음
        if (c == -1) return null; //데이터가 끝났다면 NULL 반환
        in.unread(c); // 첫 글자를 보고 판단하기 위해 다시 넣음

        if (Character.isDigit(c)) { //첫 글자가 숫자라면 비트토렌트 규약상 문자열의 시작
            return decodeBytes(); // 문자열은 byte[]로 리턴 (바이너리 대응)
        } else if (c == 'i') {
            return decodeInt();
        } else if (c == 'l') {
            return decodeList();
        } else if (c == 'd') {
            return decodeDictionary();
        }
        throw new IOException("Invalid bencode format at: " + (char)c);
    }

    // 문자열(바이너리 데이터 포함) 처리
    private byte[] decodeBytes() throws IOException {
        StringBuilder lengthStr = new StringBuilder();
        int c;
        while ((c = in.read()) != -1 && Character.isDigit(c)) {
            lengthStr.append((char) c);
        }
        if (c != ':') throw new IOException("Expected ':' in string");

        int length = Integer.parseInt(lengthStr.toString());
        byte[] data = new byte[length];
        int read = 0;
        while (read < length) {
            int r = in.read(data, read, length - read);
            if (r == -1) throw new EOFException();
            read += r;
        }
        return data;
    }

    // 2. 정수 처리
    private Long decodeInt() throws IOException {
        in.read(); // 'i' 소비
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != -1 && c != 'e') {
            sb.append((char) c);
        }
        return Long.parseLong(sb.toString());
    }

    // 3. 리스트 처리
    private List<Object> decodeList() throws IOException {
        in.read(); // 'l' 소비
        List<Object> list = new ArrayList<>();
        int c;
        while ((c = in.read()) != -1 && c != 'e') {
            in.unread(c);
            list.add(decode());
        }
        return list;
    }

    // 4. 사전 처리
    private Map<String, Object> decodeDictionary() throws IOException {
        in.read(); // 'd' 소비
        Map<String, Object> map = new TreeMap<>();
        int c;
        while ((c = in.read()) != -1 && c != 'e') {
            in.unread(c);
            // 키는 항상 문자열(Bencode 규약)
            byte[] keyBytes = decodeBytes();
            String key = new String(keyBytes, StandardCharsets.UTF_8);
            Object value = decode();
            map.put(key, value);
        }
        return map;
    }
}