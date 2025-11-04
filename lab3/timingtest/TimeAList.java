package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        // 构建规模
        AList<Integer> Ns = new AList<>();
        int x = 1000;
        for (int i = 0; i < 8; i += 1) {
            Ns.addLast(x);
            x *= 2;
        }
        // 构建时间列表
        AList<Double> times = new AList<>();
        // 总共 8 个规模，故 8 次循环
        for (int i = 0; i < Ns.size(); i += 1) {
            int n = Ns.get(i);
            AList<Integer> a = new AList<>();
            Stopwatch sw = new Stopwatch();
            // 此循环将向 a 列表添加规模数量的值，此值随便填，此处为 114514
            for (int j = 0; j < n; j += 1) {
                a.addLast(114514);
            }
            // 结束时间
            double time = sw.elapsedTime();
            // 将获得的时间添加进时间列表
            times.addLast(time);
        }
        // 总的来说，第一列和第三列是相同的
        printTimingTable(Ns, times, Ns);
    }
}
