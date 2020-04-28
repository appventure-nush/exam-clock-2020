const CHARSET = [..."ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="];
const codes = [-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1];

function base64decode(data) {
    let tempLen = data.length;
    for (let i = 0; i < data.length; i++) {
        let datum = data.charCodeAt(i);
        if ((datum > 255) || codes[datum] < 0) --tempLen;
    }
    let len = (tempLen / 4) * 3;
    if ((tempLen % 4) === 3) len += 2;
    if ((tempLen % 4) === 2) len += 1;
    let out = new Int8Array(len);
    let shift = 0;
    let accum = 0;
    let index = 0;
    for (let i = 0; i < data.length; i++) {
        let datum = data.charCodeAt(i);
        let value = (datum > 255) ? -1 : codes[datum];
        if (value >= 0) {
            accum <<= 6;
            shift += 6;
            accum |= value;
            if (shift >= 8) {
                shift -= 8;
                out[index++] = ((accum >> shift) & 0xff);
            }
        }
    }
    if (index !== out.length)
        throw new Error("Miscalculated data length (wrote " + index + " instead of " + out.length + ")");
    return out;
}

function base64encode(data) {
    let out = [];
    for (let i = 0, index = 0; i < data.length; i += 3, index += 4) {
        let quad = false;
        let trip = false;
        let val = (0xFF & data[i]);
        val <<= 8;
        if ((i + 1) < data.length) {
            val |= (0xFF & data[i + 1]);
            trip = true;
        }
        val <<= 8;
        if ((i + 2) < data.length) {
            val |= (0xFF & data[i + 2]);
            quad = true;
        }
        out[index + 3] = CHARSET[(quad ? (val & 0x3F) : 64)];
        val >>= 6;
        out[index + 2] = CHARSET[(trip ? (val & 0x3F) : 64)];
        val >>= 6;
        out[index + 1] = CHARSET[val & 0x3F];
        val >>= 6;
        out[index] = CHARSET[val & 0x3F];
    }
    return out.join('');
}