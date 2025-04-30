package io.github.albertus82.commodore.journey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.OptionalInt;
import java.util.zip.GZIPInputStream;

public class JourneyMaps {

	public static void main(final String... args) throws IOException {
		final byte[] vsf;
		try (final var is = JourneyMaps.class.getResourceAsStream("journey.vsf.gz"); final var gzis = new GZIPInputStream(is)) {
			vsf = gzis.readAllBytes();
		}

		final var baseAddr = memmem(vsf, "C64MEM".getBytes(StandardCharsets.US_ASCII)).orElseThrow(() -> new IllegalStateException("Bad VSF file"));
		final var startAddr = baseAddr + 0xA01A;
		final var endAddr = startAddr + 0x1400;

		final var maps = List.of(new char[128][84], new char[128][84]);
		var mi = 0;
		var row = 0;
		var col = 0;

		for (var i = startAddr; i <= endAddr; i++) {
			if (row != 0 && row % 128 == 0) {
				mi = mi == 0 ? 1 : 0;
				col += 2;
				row = 0;
			}
			final var map = maps.get(mi);

			// Divide the byte in 4 binary parts 00 00 00 00 that expand horizontally
			final var bin = Integer.toBinaryString(vsf[i] & 255 | 256).substring(1);
			final var e1 = bin.substring(0, 2);
			final var e2 = bin.substring(2, 4);
			final var e3 = bin.substring(4, 6);
			final var e4 = bin.substring(6);

			map[row][col] = toChar(e1);
			map[row][col + 1] = toChar(e2);
			map[row][col + 2] = toChar(e3);
			map[row][col + 3] = toChar(e4);
			row++;
		}

		final var outDir = Path.of(args != null && args.length > 0 && args[0] != null && !args[0].isBlank() ? args[0].trim() : "").toAbsolutePath();
		if (!Files.exists(outDir)) {
			Files.createDirectories(outDir);
		}
		for (int i = 0; i < maps.size(); i++) {
			final var outPath = Path.of(outDir.toString(), i == 0 ? "journey-map-current.txt" : "journey-map-original.txt");
			Files.write(outPath, new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF }); // UTF-8 BOM
			try (final var fw = Files.newBufferedWriter(outPath, StandardOpenOption.APPEND)) {
				for (final var rowArr : maps.get(i)) {
					final var rowStr = new StringBuilder();
					for (final var c : rowArr) {
						if (c == 'O') {
							rowStr.append("\u250F\u2513");
						}
						else {
							rowStr.append(c).append(c); // Double width
						}
					}
					if (!rowStr.toString().isBlank()) {
						fw.append(rowStr.toString().trim());
						fw.newLine();
					}
				}
			}
		}
	}

	private static char toChar(final String e) {
		return switch (e) {
		case "00" -> '\u2588'; // Ground
		case "01" -> '\u00A0'; // Air
		case "10" -> '\u2592'; // Water / Acid / Teleport / Sky / Special
		case "11" -> 'O'; // Object
		default -> throw new IllegalArgumentException(e);
		};
	}

	private static OptionalInt memmem(final byte[] haystack, final byte[] needle) {
		if (needle.length == 0) {
			throw new IllegalArgumentException("needle must not be empty");
		}
		for (int i = 0; i < haystack.length - needle.length + 1; ++i) {
			var found = true;
			for (var j = 0; j < needle.length; ++j) {
				if (haystack[i + j] != needle[j]) {
					found = false;
					break;
				}
			}
			if (found)
				return OptionalInt.of(i);
		}
		return OptionalInt.empty();
	}
}
