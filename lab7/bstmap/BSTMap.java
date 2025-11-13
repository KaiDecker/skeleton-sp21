package bstmap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 二叉搜索树的实现
 *
 * @author Kai Decker
 */

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    /* 二叉搜索树的节点类 */
    private class BSTNode {

        private final K key;
        private final V value;
        private BSTNode left;
        private BSTNode right;

        private BSTNode(K key, V value) {

            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
        }

        /**
         * 用于中序遍历该节点的子树
         * @return 返回一个包含节点的列表
         */
        private List<BSTNode> nodesInOrder() {
            List<BSTNode> keys = new ArrayList<>();
            if (left != null) {
                keys.addAll(left.nodesInOrder());
            }
            keys.add(this);
            if (right != null) {
                keys.addAll(right.nodesInOrder());
            }
            return keys;
        }
    }
    /* 二叉搜索树的根节点 */
    private BSTNode root;
    /* 存储映射中键值对的数量 */
    private int size;
    /* 初始二叉树为空 */
    public BSTMap() {
        clear();
    }

    /**
     * 清空树的方法
     */
    @Override
    public void clear() {
        this.root = null;
        this.size = 0;
    }

    /**
     * 检查树中是否存在指定的键 key
     *
     * @param key 指定的键
     * @return 如果找到则返回 true
     */
    @Override
    public boolean containsKey(K key) {
        if (root == null) {
            return false;
        }
        /* 通过调用 bSearch 方法 */
        Object[] obj = bSearch(this.root, key);
        return obj[2].equals(0);
    }

    /**
     * 用于获得与键对应的值 value
     *
     * @param key 指定的键
     * @return 如果键存在则返回值 value，否则返回 null
     */
    @Override
    public V get(K key) {
        if (root == null) {
            return null;
        }
        Object[] obj = bSearch(this.root, key);
        BSTNode node = (BSTNode) obj[1];
        if (obj[2].equals(0)) {
            return node.value;
        }
        return null;
    }

    /**
     * @return 返回二叉搜索树中键值对的数量
     */
    @Override
    public int size() {
        return this.size;
    }

    /**
     * 用于插入或更新键值对
     * 如果键已经存在，则什么也不做
     * 否则，根据键与当前节点的比较结果，递归插入到左子树或右子树
     *
     * @param key 指定的键
     * @param value 指定的值
     */
    @Override
    public void put(K key, V value) {
        if (root == null) {
            this.root = new BSTNode(key, value);
            this.size++;
            return;
        }
        BSTNode node = this.root;
        Object[] obj = bSearch(node, key);
        node = (BSTNode) obj[1];
        if (obj[2].equals(0)) {
            return;
        }
        if (obj[2].equals(-1)) {
            node.left = new BSTNode(key, value);
        } else {
            node.right = new BSTNode(key, value);
        }
        this.size++;
    }

    /**
     * 用于返回二叉搜索树中所有键的集合
     * 通过中序遍历获得所有节点的键
     *
     * @return 二叉搜索树中所有键的集合 Set
     */
    @Override
    public Set<K> keySet() {
        if (this.size == 0) {
            return null;
        }
        List<K> keys = new ArrayList<>();
        for (BSTNode node : this.root.nodesInOrder()) {
            keys.add(node.key);
        }
        return Set.copyOf(keys);
    }

    /**
     * 从二叉搜索树中删除一个给定键对应的键值对
     * 即删除了一个节点
     *
     * @param key 指定的键
     * @return 被删除节点的值
     */
    @Override
    public V remove(K key) {
        Object[] obj = bSearch(this.root, key);
        /* 节点 node 的父节点 */
        BSTNode prev = (BSTNode) obj[0];
        /* 要查找的目标节点 */
        BSTNode node = (BSTNode) obj[1];
        BSTNode rm = node;
        /* 保存要删除节点的值 */
        V ret = rm.value;
        /* 如果 node 是树的根节点，则调用 removeRoot() 方法 */
        if (node == this.root) {
            removeRoot(node, rm);
        /* 如果 node 没有子节点，即 node 为叶子节点 */
        } else if (node.left == null && node.right == null) {
            /* 如果 node 为父节点的左子节点 */
            if (obj[2].equals(-1)) {
                prev.left = null;
            /* 如果 node 为父节点的右子节点 */
            } else {
                prev.right = null;
            }
        /*
         * 如果 node 有两个子节点，
         * 那么删除的方式通常是用 node 的右子树中最左边的节点（也称为“中序后继”）来替代 node
         * 然后删除这个替代节点
         */
        } else {
            /* 找到 node 的右子树中最左边的节点，此时 node 这个量代表替代指针 */
            node = findRightMostLeft(node, rm);
            /* 如果 node 是左子节点，则更新 prev.left = node */
            if (obj[2].equals(-1)) {
                prev.left = node;
            /* 如果是右子节点，则更新 prev.right = node */
            } else {
                prev.right = node;
            }
        }
        this.size--;
        return ret;
    }

    @Override
    public V remove(K key, V value) {
        return remove(key);
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    public void printInOrder() {
        StringBuilder s = new StringBuilder();
        for (BSTNode node : this.root.nodesInOrder()) {
            s.append(node.value.toString());
        }
        System.out.print(s);
    }

    /**
     * 二叉搜索树中的搜索方法
     * 查找指定键的节点
     * 采用迭代的方法
     *
     * @param node 当前节点
     * @param key 指定的键
     * @return 一个 Object[] 数组
     * 包含[prev, node, 整数值]
     * prev: 当前节点的父节点（即上一个访问的节点）
     * node: 当前查找的节点，表示最终定位到的节点
     * 一个整数值，表示查找的结果状态：
     * -1: 如果目标键应该插入到当前节点的左子树中（即当前节点比目标键大）
     * 1: 如果目标键应该插入到当前节点的右子树中（即当前节点比目标键小）
     * 0: 如果目标键已经找到了，且当前节点就是目标节点
     */
    private Object[] bSearch(BSTNode node, K key) {
        /* prev 用于记录当前节点的父节点 */
        BSTNode prev = null;
        while (true) {
            /* 如果当前节点的 key 大于目标键 key */
            if (node.key.compareTo(key) > 0) {
                /* 如果当前节点的左子节点不为空 */
                if (node.left != null) {
                    /* 将当前节点当作父节点 */
                    prev = node;
                    /* 继续查找左子树 */
                    node = node.left;
                /* 如果当前节点的左子树为空 */
                } else {
                    /* 返回当前节点的父节点、当前节点和 -1，表示要插入到左子树 */
                    return new Object[]{prev, node, -1};
                }
            /* 如果当前节点的 key 小于目标键 key */
            } else if (node.key.compareTo(key) < 0) {
                /* 如果当前节点的右子节点不为空 */
                if (node.right != null) {
                    /* 将当前节点作为父节点 */
                    prev = node;
                    /* 继续查找右子树 */
                    node = node.right;
                /* 如果当前节点的右子树为空 */
                } else {
                    /* 返回当前节点的父节点、当前节点和 1，表示要插入到右子树 */
                    return new Object[]{prev, node, 1};
                }
            /* 如果当前节点的 key 等于目标键 key */
            } else {
                /* 返回当前节点的父节点、当前节点和 0，表示找到了目标节点 */
                return new Object[]{prev, node, 0};
            }
        }
    }

    /**
     * 当一个节点有两个子节点时，用于如何选择一个替代节点来保持树的结构
     * 即找到一个节点的右子树中最左边的节点
     *
     * @param node 目标节点
     * @param rm 要删除的节点
     * @return
     */
    private BSTNode findRightMostLeft(BSTNode node, BSTNode rm) {
        /* 如果当前节点没有左子树 */
        if (node.left == null) {
            /* 节点本身的右子树就会成为新的节点 */
            node = node.right;
        /* 如果当前节点没有右子树 */
        } else if (node.right == null) {
            /* 节点本身的左子树将成为新的节点 */
            node = node.left;
        /* 如果当前节点有左右子树 */
        } else {
            /* 从当前节点的左子树开始查找替代节点 */
            node = node.left;
            /* 如果左子树中最右边的节点存在 */
            if (node.right != null) {
                /* 用于记录最右节点的父节点 */
                BSTNode pn = null;
                /* 遍历 */
                while (node.right != null) {
                    pn = node;
                    node = node.right;
                }
                /* 调整左子树的连接 */
                pn.right = node.left;
                node.left = rm.left;
            }
            node.right = rm.right;
        }
        return node;
    }

    /**
     * 用于根节点的删除操作
     *
     * @param node 当前的根节点
     * @param rm 被移除的根节点的引用
     */
    private void removeRoot(BSTNode node, BSTNode rm) {
        /* 如果根节点有左子树，那么我们需要找到一个合适的节点替代根节点，并保持树的有序性 */
        if (node.left != null) {
            node = findRightMostLeft(node, rm);
            node.right = rm.right;
            this.root = rm.left;
        /* 如果根节点没有左子树 */
        } else {
            this.root = this.root.right;
        }
    }
}
