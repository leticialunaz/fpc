import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.io.BufferedReader;
import java.io.FileReader;

public class DnaConcurrentMain {

    static int contador = 0;
    static Semaphore mutex = new Semaphore(1);
    static ArrayList<Thread> threads = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso: java DnaConcurrentMain DIRETORIO_ARQUIVOS PADRAO");
            System.err.println("Exemplo: java DnaConcurrentMain dna_inputs CGTAA");
            System.exit(1);
        }

        String dirName = args[0];
        String pattern = args[1];

        File dir = new File(dirName);
        if (!dir.isDirectory()) {
            System.err.println("Caminho não é um diretório: " + dirName);
            System.exit(2);
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            System.err.println("Nenhum arquivo .txt encontrado em: " + dirName);
            System.exit(3);
        }

        try {            
            for (File f : files) {
                Work work = new Work(f, pattern);
                Thread t = new Thread(work);
                t.start();
                threads.add(t);
            }

            for(Thread t : threads){
                try{
                    t.join();
                } catch(Exception e){
                    System.out.println("Deu errado");
                }
            }

	        System.out.println("Sequência " + pattern + " foi encontrada " + contador + " vezes.");
        } catch (Exception e) {
            System.err.println("Erro ao ler arquivos: " + e.getMessage());
            System.exit(4);
        }
    }


    public static long countInFile(File file, String pattern) throws IOException {
        long total = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                
		// simulando delay de processamento
		try {
             	   Thread.sleep(10);
           	} catch (InterruptedException e) {
                   Thread.currentThread().interrupt();
                }

		if (!line.isEmpty()) {
                    total += countInSequence(line, pattern);
                }
            }
        }
        return total;    
    }

    public static long countInSequence(String sequence, String pattern) {
        if (sequence == null || pattern == null) {
            return 0;
        }
        int n = sequence.length();
        int m = pattern.length();
        if (m == 0 || n < m) {
            return 0;
        }
        long count = 0;
        for (int i = 0; i <= n - m; i++) {
            if (sequence.regionMatches(false, i, pattern, 0, m)) {
                count++;
            }
        }
        return count;
    }

}


class Work implements Runnable{
    public File file;
    public String pattern;

    public Work(File file, String pattern){
        this.file = file;
        this.pattern = pattern;
    }

    @Override
    public void run(){
        try{
            long n;
            n = DnaConcurrentMain.countInFile(file, pattern);

            DnaConcurrentMain.mutex.acquire();
            try{
                DnaConcurrentMain.contador += n;
            } finally{
                DnaConcurrentMain.mutex.release();
            }
        } catch (Exception e){
            System.out.println("Deu errado!");
        }
    }
}