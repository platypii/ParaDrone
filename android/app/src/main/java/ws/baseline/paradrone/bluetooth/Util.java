package ws.baseline.paradrone.bluetooth;

import androidx.annotation.NonNull;

class Util {

    /**
     * Convert a byte array into a human readable hex string.
     * "foo".getBytes() -> "66-6f-6f"
     */
    @NonNull
    static String byteArrayToHex(@NonNull byte[] a) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                sb.append('-');
            }
            sb.append(String.format("%02x", a[i]));
        }
        return sb.toString();
    }

}
