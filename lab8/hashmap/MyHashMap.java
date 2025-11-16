package hashmap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 一个基于哈希表实现的映射提供常数时间的元素访问
 * 理想情况下，通过 get(), remove() 和 put() 方法实现
 * <p>
 * 假定永远不会插入为 null 的键，并且在 remove() 时不会缩小容量
 *
 * @author Kai Decker
 */

public class MyHashMap<K, V> implements Map61B<K, V> {

    /* 负载因子阈值 */
    private final double loadFactor;
    /* 当前桶数组长度，即 bucket 数量 */
    private int size;
    /* 当前存了多少个键值对 */
    private int length;
    /* 存放桶的数组，每个桶是一堆 Node */
    private Collection<Node>[] buckets;

    /**
     * 受保护的辅助类，用于存储键/值对
     * 受保护修饰符允许子类访问此类
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* 构造方法 */
    public MyHashMap() {
        size = 16;
        loadFactor = 0.75;
        length = 0;
        buckets = createTable(size);
    }

    /**
     * 可允许自定义初始桶的数量
     *
     * @param initialSize 桶数组初始大小
     */
    public MyHashMap(int initialSize) {
        size = initialSize;
        loadFactor = 0.75;
        length = 0;
        buckets = createTable(size);
    }

    /**
     * 可自定义初始桶的数量和最大负载因子
     * 负载因子为(元素数量 / 桶数量)
     *
     * @param initialSize 桶数组初始大小
     * @param maxLoad 最大负载因子
     */
    public MyHashMap(int initialSize, double maxLoad) {
        size = initialSize;
        loadFactor = maxLoad;
        length = 0;
        buckets = createTable(size);
    }

    /**
     * 返回要放入哈希表桶中的新节点
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * 返回用作哈希表桶的数据结构
     * <p>
     * 哈希表桶的唯一要求是我们可以：
     * 1. 插入元素('add' 方法)
     * 2. 删除元素('remove' 方法)
     * 3. 遍历元素('iterator' 方法)
     * <p>
     * java.util.Collection 支持所有这些方法
     * Java 中的大多数数据结构都继承自 Collection
     * 因此我们可以使用几乎任何数据结构作为我们的桶
     * <p>
     * 重写此方法可以使用不同的数据结构作为底层桶类型
     * <p>
     * 请务必调用此工厂方法，而不是使用 new 操作符创建自己的桶数据结构
     */
    protected Collection<Node> createBucket() {
        return new HashSet<>();
    }

    /**
     * 返回支撑我们哈希表的底层数组
     * 根据上述说明，此表可以是一个 Collection 对象数组
     * <p>
     * 创建表时请务必调用此工厂方法
     * 以确保所有桶类型都是 java.util.Collection
     *
     * @param tableSize 要创建的表的尺寸
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    /**
     * 清空的方法
     * 重新分配一个长度为 16 的桶数组
     */
    @Override
    public void clear() {
        size = 16;
        length = 0;
        buckets = createTable(size);
    }

    /**
     * 检查给定的键是否存在
     * 直接用 get 方法看有没有对应值，因为在 lab8 中不考虑 null
     *
     * @param key 给定的键
     */
    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    /**
     * 获取值的方法
     *
     * @param key 给定的键
     * @return 对应的值，若找不到或该键没存放，则返回 null
     */
    @Override
    public V get(K key) {
        int pos = getPosition(key);
        Collection<Node> set = buckets[pos];
        if (set == null) {
            return null;
        }
        for (Node p : set) {
            if (p.key.equals(key)) {
                return p.value;
            }
        }
        return null;
    }

    /**
     * 获取大小的方法
     *
     * @return 当前映射中键值对的数量，即 length
     */
    @Override
    public int size() {
        return length;
    }

    /**
     * 将指定键与指定值在此映射中关联
     * 如果该映射之前已包含该键的关系，
     * 则旧的值将被替换。
     *
     * @param key 给定的键
     * @param value 给定的值
     */
    @Override
    public void put(K key, V value) {
        /* 先判断是否超过负载因子 */
        if (isOverload()) {
            reSize();
        }
        /* 创建新的结点 */
        Node p = createNode(key, value);
        /* 找到对应的桶 */
        int pos = getPosition(key);
        /* 如果没桶，则创建新桶 */
        if (buckets[pos] == null) {
            buckets[pos] = createBucket();
        }
        /* 如果桶存在 */
        for (Node node : buckets[pos]) {
            /* 如果已经存在，更新 value */
            if (node.key.equals(key)) {
                node.value = value;
                return;
            }
        }
        /* 在桶里添加 */
        buckets[pos].add(p);
        length++;
    }

    /**
     * 包含映射中所有的键的集合获取方法
     *
     * @return 包含键的集合
     */
    @Override
    public Set<K> keySet() {
        if (length == 0) {
            return null;
        }
        Set<K> ret = new HashSet<>();
        for (K k : this) {
            ret.add(k);
        }
        return ret;
    }

    /**
     * 如果存在指定键的映射关系，则从此映射中移除该映射
     * lab8 不要求实现此方法，如果未实现，请抛出 UnsupportedOperationException
     *
     * @param key 给定的键
     */
    @Override
    public V remove(K key) {
        int pos = getPosition(key);
        Collection<Node> set = buckets[pos];
        if (set == null) {
            return null;
        }
        /* 遍历删除 */
        for (Node p : set) {
            if (p.key.equals(key)) {
                set.remove(p);
                return p.value;
            }
        }
        return null;
    }

    /**
     * 仅当指定键当前映射到指定值时，才移除该键的条目
     * lab8 不要求实现此方法。如果未实现，请抛出 UnsupportedOperationException
     *
     * @param key 给定的键
     * @param value 给定的值
     */
    @Override
    public V remove(K key, V value) {
        int pos = getPosition(key);
        Node p = createNode(key, value);
        Collection<Node> set = buckets[pos];
        if (set == null || !set.contains(p)) {
            return null;
        }
        buckets[pos].remove(p);
        return value;
    }

    /**
     * 返回一个用于遍历 {@code T} 类型元素的迭代器
     *
     * @return 迭代器对象
     */
    @Override
    public Iterator<K> iterator() {
        return new Iterator<>() {
            private final Collection<Node>[] b = buckets;
            private int pos = findPos(0);

            private Collection<Node> curBuck = b[pos];
            private Iterator<Node> curIter = curBuck.iterator();

            /**
             * 从某个下标开始找下一个非空桶
             *
             * @param cur 给定的下标起点
             * @return 下一个非空桶的下标
             */
            private int findPos(int cur) {
                int pos = cur;
                while (pos < size && b[pos] == null) {
                    pos++;
                }
                return pos;
            }

            /**
             * 判断当前桶里是否还有元素或者后面是否还能找到非空桶
             *
             * @return 有元素的话返回 true
             */
            @Override
            public boolean hasNext() {
                return curIter.hasNext() || findPos(pos + 1) < size;
            }

            @Override
            public K next() {
                if (curIter.hasNext()) {
                    Node curNode = curIter.next();
                    return curNode.key;
                }
                pos = findPos(pos + 1);
                curBuck = b[pos];
                curIter = curBuck.iterator();
                return curIter.next().key;
            }
        };
    }

    /**
     * 扩容的方法
     */
    private void reSize() {
        /* 新建一个更大的临时 MyHashMap 大小为原来的两倍 */
        MyHashMap<K, V> temp = new MyHashMap<>(size * 2);
        /* 遍历放入元素 */
        for (K key : this) {
            temp.put(key, get(key));
        }
        size *= 2;
        /* 改变之前的 buckets 指针 */
        buckets = temp.buckets;
    }

    /**
     * 判断是否需要扩容
     * 如果 元素个数 / 桶数量 ≥ 负载因子阈值，就要扩容
     *
     * @return 需要扩容这返回 true
     */
    private boolean isOverload() {
        return (double) length / size >= loadFactor;
    }

    /**
     * 通过计算得知给定的键应该去哪个桶
     *
     * @param key 给定的键
     * @return 对应的哈希值
     */
    private int getPosition(K key) {
        return Math.floorMod(key.hashCode(), size);
    }
}
