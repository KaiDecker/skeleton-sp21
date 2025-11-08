package tester;

import static org.junit.Assert.*;
import org.junit.Test;
import student.StudentArrayDeque;

import edu.princeton.cs.algs4.StdRandom;

public class TestArrayDequeEC {
    @Test
    public void testFailed() {
        // 存储学生创建的队列
        StudentArrayDeque<Integer> student = new StudentArrayDeque<>();
        // 存储正确实现的队列
        ArrayDequeSolution<Integer> solution = new ArrayDequeSolution<>();
        // 初始调试信息
        String msg = "";
        // 决定测试循环要执行多少次操作
        for (int i = 0; i < StdRandom.uniform(0, 1000000); i++) {
            // 确定选择测试的方法
            double choice = StdRandom.uniform();
            // 用于测试方法的参数
            Integer randVal = StdRandom.uniform(0, 100);
            if (choice < 0.33) {
                student.addLast(randVal);
                solution.addLast(randVal);
                msg += "addLast(" + randVal + ")\n";
            } else if (choice < 0.67) {
                student.addFirst(randVal);
                solution.addFirst(randVal);
                msg += "addFirst(" + randVal + ")\n";
            } else {
                int size = student.size();
                msg += "size()\n";
                if(size > 0){
                    if(randVal <50){
                        msg+="removeFirst()\n";
                        assertEquals(msg,solution.removeFirst(),student.removeFirst());
                    } else {
                        msg +="removeLast()\n";
                        assertEquals(msg,solution.removeLast(),student.removeLast());
                    }
                }
            }
        }
    }
}
