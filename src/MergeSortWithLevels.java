import java.util.Arrays;
import java.util.Random;

public class MergeSortWithLevels {
	private static final Random RAND = new Random(42); // random number generator

	public static void main(String[] args) throws Throwable {
		int LENGTH = 1000; // tamanho do vetor
		int RUNS = 16; // quantas execuções serão testadas?

		for (int i = 1; i <= RUNS; i++) {
			int[] a = createRandomArray(LENGTH);

			// Executa o algoritmo e verifica quanto tempo ele leva.
			long startTime1 = System.currentTimeMillis();
			parallelMergeSort(a);
			long endTime1 = System.currentTimeMillis();

			if (!isSorted(a)) {
				throw new RuntimeException("not sorted afterward: " + Arrays.toString(a));
			}

			System.out.printf("%10d elements  =>  %6d ms \n", LENGTH, endTime1 - startTime1);
			LENGTH *= 2; // dobra o tamanho do array para a próxima execução.
		}
	}

	public static void parallelMergeSort(int[] a) {
		int LEVEL = 3;
		parallelMergeSort(a, LEVEL); // 3 levels => 2^3=8 threads
	}

	public static void parallelMergeSort(int[] a, int level) {
		if (a.length < 2) {
			return;
		}

		if (level == 0) {
			mergeSort(a);
			return;
		}
		// Divide o array ao meio.
		int[] left = Arrays.copyOfRange(a, 0, a.length / 2);
		int[] right = Arrays.copyOfRange(a, a.length / 2, a.length);

		// Ordena ambas as metades.
		// mergeSort(left);
		// mergeSort(right);
		Thread lThread = new Thread(new MergeSortRunner(left, level - 1));
		Thread rThread = new Thread(new MergeSortRunner(right, level - 1));
		lThread.start();
		rThread.start();

		try {
			lThread.join();
			rThread.join();
		} catch (InterruptedException ie) {
		}

		// Junta as metades do vetor.
		merge(left, right, a);

	}

	/*
	 * Rearranja os elementos do vetor de forma ordenada usando o algoritmo
	 * MergeSort. MergeSort é O(N log N) para todas as entradas.
	 */
	public static void mergeSort(int[] a) {
		if (a.length >= 2) {
			// Divide o array ao meio.
			int[] left = Arrays.copyOfRange(a, 0, a.length / 2);
			int[] right = Arrays.copyOfRange(a, a.length / 2, a.length);

			// Ordena ambas as metades.
			mergeSort(left);
			mergeSort(right);

			// Junta as metades do vetor.
			merge(left, right, a);
		}
	}

	/*
	 * Combina os elementos ordenados das metades direita/esquerda do vetor no vetor
	 * prnicipal. De modo que left.length + right.length == a.length.
	 */
	public static void merge(int[] left, int[] right, int[] a) {
		int i1 = 0;
		int i2 = 0;
		for (int i = 0; i < a.length; i++) {
			if (i2 >= right.length || (i1 < left.length && left[i1] < right[i2])) {
				a[i] = left[i1];
				i1++;
			} else {
				a[i] = right[i2];
				i2++;
			}
		}
	}

	// Troca os valores entre duas posições do vetor.
	public static final void swap(int[] a, int i, int j) {
		if (i != j) {
			int temp = a[i];
			a[i] = a[j];
			a[j] = temp;
		}
	}

	// Randomicamente rearranja os elementos de um vetor.
	public static void shuffle(int[] a) {
		for (int i = 0; i < a.length; i++) {
			// move element i to a random index in [i .. length-1]
			int randomIndex = (int) (Math.random() * a.length - i);
			swap(a, i, i + randomIndex);
		}
	}

	// Verifica se o vetor está completamente ordenado em ordem crescente.
	public static boolean isSorted(int[] a) {
		for (int i = 0; i < a.length - 1; i++) {
			if (a[i] > a[i + 1]) {
				return false;
			}
		}
		return true;
	}

	// Cria um vetor de tamanho length e o preenche com valores não-negativos
	// inteiros.
	public static int[] createRandomArray(int length) {
		int[] a = new int[length];
		for (int i = 0; i < a.length; i++) {
			a[i] = RAND.nextInt(100);
			// a[i] = RAND.nextInt(40);
		}
		return a;
	}
}

/*
 * A classe Sorter representa uma tarefa que pode ser executada em uma thread
 * Ela realiza a execução do MergeSort em um array. A ideia é que o agoritmo
 * paralelo do MergeSort possa criar vários "Ordenadores", cada um para um
 * pequeno espaço do array. Ao final, uma thread irá unir as pequenas partes
 * ordenando o vetor todo.
 */

class MergeSortRunner implements Runnable {
	private int[] a;
	private int level;

	public MergeSortRunner(int[] a, int level) {
		this.a = a;
		this.level = level;
	}

	public void run() {
		MergeSortWithLevels.parallelMergeSort(a, level);
	}
}
