package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private int length;
    private int size;
    private int first;
    private int last;
    private T[] arr;

    public ArrayDeque() {
        this.arr = (T[]) new Object[8];
        this.size = 8;
        this.length = 0;
        this.first = 0;
        this.last = 0;
    }

    @Override
    public void addFirst(T item) {
        if (this.length == this.size - 1) {
            reSize(this.size * 2);
        }
        if (!isEmpty()) {
            // 由于使用的是数组来实现，采用的是类似圆的循环，需要取模进行运算
            // 即在某些时候可视化中数组中的第一个数据不一定在 arr[0]
            this.first = (this.first - 1 + this.size) % this.size;
        }
        this.arr[first] = item;
        this.length++;
    }

    @Override
    public void addLast(T item) {
        if (this.length == this.size - 1) {
            reSize(this.size * 2);
        }
        if (!isEmpty()) {
            this.last = (this.last + 1) % this.size;
        }
        this.arr[this.last] = item;
        this.length++;
    }

    @Override
    public int size() {
        return this.length;
    }

    @Override
    public void printDeque() {
        int i = this.first;
        while (i != this.last) {
            // 遍历数组进行打印
            System.out.print(this.arr[i] + " ");
            i = (i + 1) % this.size;
        }
        System.out.println(this.arr[i]);
    }

    @Override
    public T removeFirst() {
        // 如果数组为空，则直接停止
        if (isEmpty()) {
            return null;
        }
        // 长度减少
        this.length--;
        // ret 指向第一个元素
        T ret = this.arr[this.first];
        // 将 first 重新指向下一个元素
        this.first = (this.first + 1) % this.size;
        // 若空闲的空间太多，则减少空间
        if (this.length * 4 < this.size) {
            reSize(this.size / 2);
        }
        // 此方法返回原来的第一个元素
        return ret;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        this.length--;
        T ret = this.arr[this.last];
        this.last = (this.last - 1 + this.size) % this.size;
        if (this.length * 4 < this.size) {
            reSize(this.size / 2);
        }
        return ret;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= this.length) {
            return null;
        }
        int position = (index + this.first) % this.size;
        return this.arr[position];
    }

    private void reSize(int s) {
        T[] temp = (T[]) new Object[s];
        int i = this.first;
        int j = 0;
        while (j < this.length) {
            // 将当前元素放到新数组
            temp[j] = this.arr[i];
            i = (i + 1) % this.size;
            j++;
        }
        this.size = s;
        this.first = 0;
        if (isEmpty()) {
            this.last = 0;
        } else {
            this.last = j - 1;
        }
        this.arr = temp;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Deque) || ((Deque<?>) o).size() != this.size()) {
            return false;
        }
        if (o == this) {
            return true;
        }
        for (int i = 0; i < this.size(); i++) {
            Object item = ((Deque<?>) o).get(i);
            if (!(this.get(i).equals(item))) {
                return false;
            }
        }
        return true;
    }

    public Iterator<T> iterator() {
        return new Iterable();
    }

    private class Iterable implements Iterator<T> {
        private int pos;

        Iterable() {
            this.pos = 0;
        }

        public boolean hasNext() {
            return this.pos < length;
        }

        public T next() {
            T ret = get(this.pos);
            this.pos++;
            return ret;
        }
    }
}
