//importando a classe random
import java.util.Random;

public class ArraySum {
	private static final Random RAND = new Random(42); // random number generator

	public static void main(String[] args) throws Throwable {
		int LENGTH = 1000; // tamanho inicial do vetor
		int RUNS = 15; // Quantas vezes irá crescer * 2 ?
		int qntThreads = 1; // determina a quantidade de threads a serem criadas.

		for (int i = 1; i <= RUNS; i++) {
			int[] a = createRandomArray(LENGTH);

			// Executa o algoritmo e verifica o tempo de execução.
			long startTime1 = System.currentTimeMillis();
			int total = 0;
			for (int j = 1; j <= 100; j++) {

				if (qntThreads > LENGTH / 2) {
					throw new RuntimeException("Quantidade inútil de threads criadas.");
				} else {

					if (qntThreads == 1) {
						total = sequentialSum(a);
					} else if (qntThreads == 2) {
						total = parallelSumWith2summers(a);
					} else {
						total = parallelSumWithNsummers(a, qntThreads);
					}
				}
			}
			long endTime1 = System.currentTimeMillis();

			int correct = sequentialSum(a);
			if (total != correct) {
				throw new RuntimeException("wrong sum: " + total + " vs. " + correct);
			}

			System.out.printf("%10d elements  =>  %6d ms \n", LENGTH, endTime1 - startTime1);
			LENGTH *= 2; // Duplica o tamanho do vetor para a próxima execução
		}
	}

	// Realiza soma sequencial
	public static int sequentialSum(int[] a) {
		int result = 0;
		for (int i = 0; i < a.length; i++) {
			result += a[i];
		}
		return result;
	}

	// Realiza a soma dos elementos com 2 threads.
	public static int parallelSumWith2summers(int[] a) {
		Summer leftSummer = new Summer(a, 0, a.length / 2);
		Summer rightSummer = new Summer(a, a.length / 2, a.length);
		Thread leftThread = new Thread(leftSummer);
		Thread rightThread = new Thread(rightSummer);

		// executa as threads
		leftThread.start();
		rightThread.start();

		// esperam a execução da threads finalizar
		try {
			leftThread.join();
			rightThread.join();
		} catch (InterruptedException ie) {
		}

		// combina os resultados de ambas as threads.
		int left = leftSummer.getSum();
		int right = rightSummer.getSum();
		return left + right;
	}

	// Realiza a soma dos elementos com N (threadCount) threads.
	public static int parallelSumWithNsummers(int[] a, int threadCount) {
		int len = (int) Math.ceil(1.0 * a.length / threadCount);
		Summer[] summers = new Summer[threadCount];
		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threadCount; i++) {
			summers[i] = new Summer(a, i * len, Math.min((i + 1) * len, a.length));
			threads[i] = new Thread(summers[i]);
			threads[i].start();
		}
		try {
			for (Thread t : threads) {
				t.join();
			}
		} catch (InterruptedException ie) {
		}

		int total = 0;
		for (Summer summer : summers) {
			total += summer.getSum();
		}
		return total;
	}

	// Método auxiliar para computar a soma dos elementos de uma porção [min ...
	// max] do vetor
	public static int sumRange(int[] a, int min, int max) {
		int result = 0;
		for (int i = min; i < max; i++) {
			result += a[i];
		}
		return result;
	}

	// Preenche o vetor com elementos aleatórios.
	public static int[] createRandomArray(int length) {
		int[] a = new int[length];
		for (int i = 0; i < a.length; i++) {
			a[i] = RAND.nextInt(50);
		}
		return a;
	}
}

class Summer implements Runnable {
	private int[] a;
	private int min, max;
	private int sum;

	public Summer(int[] a, int min, int max) {
		this.a = a;
		this.min = min;
		this.max = max;
	}

	public int getSum() {
		return sum;
	}

	public void run() {
		this.sum = ArraySum.sumRange(a, min, max);
	}
}
