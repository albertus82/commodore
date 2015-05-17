package it.albertus.commodore.missione;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PrintMap {

	public static void main(String... args) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream("c:/users/alberto/desktop/c64/missione.vsf"));
		byte[] file = new byte[69794];
		is.read(file);
		is.close();

//		int inizio = 0x8e84;

		int inizio = 41092;

		int fine = 0xB483;

		char mappa[][] = new char[128][128];
		int row = 0;
		int col = 0;

		for (int i = inizio; i <= fine; i++) {

			if (row != 0 && row % 128 == 0) {
				i += 128;
				row = 0;
				col += 4;
			}

			// dividere il byte in 4 parti binarie 00 00 00 00 che vanno in orizzontale
			String bin = Integer.toBinaryString(file[i] & 255 | 256).substring(1);
			String e1 = bin.substring(0, 2);
			String e2 = bin.substring(2, 4);
			String e3 = bin.substring(4, 6);
			String e4 = bin.substring(6);

			mappa[row][col] = toChar(e1);
			mappa[row][col + 1] = toChar(e2);
			mappa[row][col + 2] = toChar(e3);
			mappa[row][col + 3] = toChar(e4);
			row++;
		}


		int i = 0;
		for (char[] riga : mappa) {
			String sriga = new String(riga);
			if ( sriga.trim().length() > 0 )
				System.out.printf("%3d ", ++i);
			
			if ( sriga.trim().length() > 0 ) {
				System.out.print(sriga);
				System.out.print("\r\n");
			}
		}

	}

	private static char toChar(String e) {
		if (e.equals("00"))
			return 'O';
		else if (e.equals("01"))
			return ' ';
		else if (e.equals("10"))
			return '~';
		else if (e.equals("11"))
			return '*';
		else
			return '?';
	}
}
