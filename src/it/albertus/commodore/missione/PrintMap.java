package it.albertus.commodore.missione;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PrintMap {

	public static void main(String... args) throws IOException {
		final var is = PrintMap.class.getResourceAsStream("missione.vsf");
		final var file = new byte[69794];
		is.read(file);
		is.close();

		final int startAddr = 41092;
		final int endAddr = 0xB483;

		final var map = new char[128][128];
		int row = 0;
		int col = 0;

		for (int i = startAddr; i <= endAddr; i++) {
			if (row != 0 && row % 128 == 0) {
				i += 128;
				row = 0;
				col += 4;
			}

			// dividere il byte in 4 parti binarie 00 00 00 00 che vanno in orizzontale
			var bin = Integer.toBinaryString(file[i] & 255 | 256).substring(1);
			var e1 = bin.substring(0, 2);
			var e2 = bin.substring(2, 4);
			var e3 = bin.substring(4, 6);
			var e4 = bin.substring(6);

			map[row][col] = toChar(e1);
			map[row][col + 1] = toChar(e2);
			map[row][col + 2] = toChar(e3);
			map[row][col + 3] = toChar(e4);
			row++;
		}

		final var outPath = Path.of("map.txt");
		Files.write(outPath, new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
		try (final var fw = Files.newBufferedWriter(outPath, StandardOpenOption.APPEND)) {
			var ln = 0;
			for (final var riga : map) {
				final var sriga = new StringBuilder();
				for (final var c : riga) {
					sriga.append(c).append(c);
				}

				if (!sriga.isEmpty()) {
					final var sln = String.format("%3d ", ++ln);
					fw.append(sln);
					fw.append(sriga);
					fw.newLine();
				}
			}
		}
	}

	private static char toChar(final String e) {
		if (e.equals("00")) {
			return '\u2588';
		}
		else if (e.equals("01")) {
			return ' ';
		}
		else if (e.equals("10")) {
			return '~';
		}
		else if (e.equals("11")) {
			return '*';
		}
		else {
			return '?';
		}
	}
}
