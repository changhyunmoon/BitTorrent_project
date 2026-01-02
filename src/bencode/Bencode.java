package bencode;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class Bencode {
    public static void encode(Object o, OutputStream out) throws IOException {
        if (o instanceof byte[]) {
            byte[] b = (byte[]) o;
            out.write(Integer.toString(b.length).getBytes(StandardCharsets.UTF_8));
            out.write(':');
            out.write(b);
        } else if (o instanceof String) {
            encode(((String) o).getBytes(StandardCharsets.UTF_8), out);
        } else if (o instanceof Number) {
            out.write('i');
            out.write(o.toString().getBytes(StandardCharsets.UTF_8));
            out.write('e');
        } else if (o instanceof List) {
            out.write('l');
            for (Object item : (List<?>) o) encode(item, out);
            out.write('e');
        } else if (o instanceof Map) {
            out.write('d');
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) o;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                encode(entry.getKey(), out);
                encode(entry.getValue(), out);
            }
            out.write('e');
        }
    }
}