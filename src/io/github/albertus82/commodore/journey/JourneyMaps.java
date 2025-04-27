package io.github.albertus82.commodore.journey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.OptionalInt;

public class JourneyMaps {

	public static void main(final String... args) throws IOException {
		final byte[] dump;
		try (final var is = JourneyMaps.class.getResourceAsStream("journey.vsf")) {
			dump = is.readAllBytes();
		}

		final var baseAddr = memmem(dump, "C64MEM".getBytes(StandardCharsets.US_ASCII)).orElseThrow();
		final var startAddr = baseAddr + 0xA01A;
		final var endAddr = startAddr + 5120;

		final var maps = new char[][][] { new char[128][84], new char[128][84] };
		var mi = 0;
		var row = 0;
		var col = 0;

		for (var i = startAddr; i <= endAddr; i++) {
			if (row != 0 && row % 128 == 0) {
				mi = mi == 0 ? 1 : 0;
				col += 2;
				row = 0;
			}
			final var map = maps[mi];

			// dividere il byte in 4 parti binarie 00 00 00 00 che vanno in orizzontale
			final var bin = Integer.toBinaryString(dump[i] & 255 | 256).substring(1);
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

		for (int i = 0; i < maps.length; i++) {
			final var outPath = Path.of(i == 0 ? "current.txt" : "original.txt");
			Files.write(outPath, new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
			try (final var fw = Files.newBufferedWriter(outPath, StandardOpenOption.APPEND)) {
				for (final var rowArr : maps[i]) {
					final var rowStr = new StringBuilder();
					for (final var c : rowArr) {
						rowStr.append(c).append(c);
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
		if (e.equals("00")) {
			return '\u2588';
		}
		else if (e.equals("01")) {
			return '\u00A0';
		}
		else if (e.equals("10")) {
			return '\u2592';
		}
		else if (e.equals("11")) {
			return '\u25A0';
		}
		else {
			throw new IllegalArgumentException(e);
		}
	}

	private static OptionalInt memmem(final byte[] haystack, final byte[] needle) {
		if (needle.length == 0) {
			throw new IllegalArgumentException("needle must not be empty");
		}
		for (int i = 0; i < haystack.length - needle.length + 1; ++i) {
			boolean found = true;
			for (int j = 0; j < needle.length; ++j) {
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
