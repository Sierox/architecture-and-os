package ku.syucel17.comp304.proj3;

import ku.syucel17.comp304.proj3.model.FileSystem;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        // CONTIGUOUS TESTS
        Test t0 = new Test("C:\\Users\\HP\\Desktop\\input_8_600_5_5_0.txt", FileSystem.CONTIGUOUS);
        Test t1 = new Test("C:\\Users\\HP\\Desktop\\input_1024_200_5_9_9.txt", FileSystem.CONTIGUOUS);
        Test t2 = new Test("C:\\Users\\HP\\Desktop\\input_1024_200_9_0_0.txt", FileSystem.CONTIGUOUS);
        Test t3 = new Test("C:\\Users\\HP\\Desktop\\input_1024_200_9_0_9.txt", FileSystem.CONTIGUOUS);
        Test t4 = new Test("C:\\Users\\HP\\Desktop\\input_2048_600_5_5_0.txt", FileSystem.CONTIGUOUS);
        Test[] contTests = {t0, t1, t2, t3, t4};

        System.out.println("=============================");
        for (int t = 0; t < 5; t++) {
            long totalTime = 0;
            for (int i = 0; i < 5; i++) {
                contTests[t].run();
                totalTime += contTests[t].getRuntime();
                if(i!=4)
                    contTests[t].reset();
            }
            System.out.println("Test #" + t + " (Contiguous)\n" +
                    "Create rejects: " + contTests[t].getCreateRejectCounter() + "\n" +
                    "Extend rejects: " + contTests[t].getExtendRejectCounter() + "\n" +
                    "Access rejects: " + contTests[t].getAccessRejectCounter() + "\n" +
                    "Shrink rejects: " + contTests[t].getShrinkRejectCounter() + "\n" +
                    "Average Runtime: " + totalTime / 5 + "ms" + "\n" +
                    "=============================");
        }

        // LINKED TESTS
        Test t5 = new Test("C:\\Users\\HP\\Desktop\\input_8_600_5_5_0.txt", FileSystem.LINKED);
        Test t6 = new Test("C:\\Users\\HP\\Desktop\\input_1024_200_5_9_9.txt", FileSystem.LINKED);
        Test t7 = new Test("C:\\Users\\HP\\Desktop\\input_1024_200_9_0_0.txt", FileSystem.LINKED);
        Test t8 = new Test("C:\\Users\\HP\\Desktop\\input_1024_200_9_0_9.txt", FileSystem.LINKED);
        Test t9 = new Test("C:\\Users\\HP\\Desktop\\input_2048_600_5_5_0.txt", FileSystem.LINKED);
        Test[] linkTests = {t5, t6, t7, t8, t9};

        for (int t = 0; t < 5; t++) {
            long totalTime = 0;
            for (int i = 0; i < 5; i++) {
                linkTests[t].run();
                totalTime += linkTests[t].getRuntime();
                if(i!=4)
                    linkTests[t].reset();
            }
            System.out.println("Test #" + (t+5) + " (Linked)\n" +
                    "Create rejects: " + linkTests[t].getCreateRejectCounter() + "\n" +
                    "Extend rejects: " + linkTests[t].getExtendRejectCounter() + "\n" +
                    "Access rejects: " + linkTests[t].getAccessRejectCounter() + "\n" +
                    "Shrink rejects: " + linkTests[t].getShrinkRejectCounter() + "\n" +
                    "Average Runtime: " + totalTime / 5 + "ms" + "\n" +
                    "=============================");
        }


    }
}
