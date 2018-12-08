package io.butoff;

import java.util.Scanner;
import java.lang.IllegalStateException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;


public class Main {

    private static final boolean TEST = true;
    private static final boolean LOG = true;
    private static final boolean SHUFFLE_ADD = false;
    private static final boolean SHUFFLE_DEL = false;
    private static final boolean REVERSE_ADD = true;
    private static final boolean REVERSE_DEL = false;

    private Node tree = null;
    private long lastSum = 0;
    private static final int MOD = 1_000_000_001;


    public static void main(String[] args) {
        if (TEST)
            new Main().test();
        else
            new Main().run();
    }

    private void test() {
        for (int i = 0; i < 1; i++) {
            tree = null;
            test_addAndDel();
//            test_addAndDelRoot();
            printTree();
        }
    }

    private List<Integer> test_list = Arrays.asList(1, 2, 3, 4, 5, 6,   8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);

    private void test_addAndDel() {
        test_add();
        List<Integer> delList = new ArrayList<>(test_list);
        if (SHUFFLE_DEL)
            Collections.shuffle(delList);
        else if (REVERSE_DEL)
            Collections.reverse(delList);
        for (int e : delList) {
            tree.del(e);
            _log("- " + e + " = ", true);
            printTree();
        }
    }

    private void test_add() {
        List<Integer> addList = new ArrayList<>(test_list);
        addList.add(7);
        if (SHUFFLE_ADD)
            Collections.shuffle(addList);
        else if (REVERSE_ADD)
            Collections.reverse(addList);
        for (int e : addList) {
            addToTree(e);
            _log("+ " + e + " = ", true);
            printTree();
        }
    }

    private void test_addAndDelRoot() {
        test_add();
        while (tree != null) {
            int e = tree.key;
            tree.del(e);
            _log("- " + e + " = ", true);
            printTree();
        }
    }

    private void addToTree(int e) {
        if (tree == null)
            tree = new Node(e, null, null, null);
        else
            tree.add(e);
    }

    private void run() {
        Scanner scanner = new Scanner(System.in);
        final int n = scanner.nextInt();
        for (int i = 0; i < n; i++) {
            String command = scanner.next();
            switch (command) {
                case "+":
                    int e = scanner.nextInt();
                    addToTree(f(e));
                    _logl("+ " + f(e) + ": ");
                    printTree();
                    break;
                case "-":
                    e = scanner.nextInt();
                    if (tree != null)
                        tree.del(f(e));
                    _logl("- " + f(e) + ": ");
                    printTree();
                    break;
                case "?":
                    e = scanner.nextInt();
                    _logl("? " + f(e) + ": ");
                    String answer = tree != null && tree.find(f(e)) ? "Found" : "Not found";
                    System.out.println(answer);
                    break;
                case "s":
                    int l = scanner.nextInt();
                    int r = scanner.nextInt();
                    _logl("s " + f(l) + " " + f(r) + ": ");
                    System.out.println(sum(l, r));
                    break;
                default:
                    throw new java.lang.IllegalArgumentException("unknown command: " + command);
            }
        }
    }

    private void printTree() {
        if (!LOG)
            return;
        System.out.print("[ ");
        if (tree != null) tree.print();
        else System.out.print("empty ");
        System.out.println("]");
    }

    private long sum(int l, int r) {
        lastSum = tree == null ? 0 : tree.sum(f(l), f(r));
        return lastSum;
    }

    private int f(int x) {
        return (int) ((x + lastSum) % MOD);
    }


    class Node {

        public int key;
        public Node left;
        public Node right;
        public Node parent;
        public int height;

        void _assertParent() {
            if (parent != null && parent.left != this && parent.right != this)
                throw new IllegalStateException("bad parent: me=" + key + ", parent=" + parent.key);
            if (left != null && left.parent != this)
                throw new IllegalStateException("bad left: me=" + key + ", left=" + left.key);
            if (right != null && right.parent != this)
                throw new IllegalStateException("bad right: me=" + key + ", right=" + left.key);
        }

        public Node(int key, Node left, Node right, Node parent) {
            this.key = key;
            this.left = left;
            this.right = right;
            this.parent = parent;
            this.height = 1;
        }

        public int lHeight() {
            return left == null ? 0 : left.height;
        }

        public int rHeight() {
            return right == null ? 0 : right.height;
        }

        public boolean find(int e) {
            if (key == e)
                return true;
            else if (e < key && left != null)
                return left.find(e);
            else if (e > key && right != null)
                return right.find(e);
            else
                return false;
        }

        public long sum(int l, int r) {
            long res = 0;
            if (key >= l && key <= r)
                res = key;
            if (key > l && left != null)
                res += left.sum(l, r);
            if (key < r && right != null)
                res += right.sum(l, r);
            return res;
        }

        public void add(int e) {
            if (e < key)
                addLeft(e);
            else if (e > key)
                addRight(e);
            // (e == tree.key) -> ignore
            balance();
            _assertParent();
        }

        private void addLeft(int e) {
            if (left == null)
                left = new Node(e, null, null, this);
            else
                left.add(e);
            if (lHeight() == height)
                height++;
            _assertParent();
        }

        private void addRight(int e) {
            if (right == null)
                right = new Node(e, null, null, this);
            else
                right.add(e);
            if (rHeight() == height)
                height++;
            _assertParent();
        }

        private void balance() {
            if (rHeight() - lHeight() > 1)
                balanceRight();
            else if (lHeight() - rHeight() > 1)
                balanceLeft();
            adjustHeight();
            _assertParent();
        }

        private void balanceLeft() {
            _logl("left-");
            if (left == null)
                throw new IllegalStateException("left tree can't be null during balance()");
            if (left.rHeight() > left.lHeight())
                balanceLeftBig();
            else
                balanceLeftSmall();
        }

        private void balanceRight() {
            _logl("right-");
            if (right == null)
                throw new IllegalStateException("right tree can't be null during balance()");
            if (right.lHeight() > right.rHeight())
                balanceRightBig();
            else
                balanceRightSmall();
        }

        private void balanceLeftSmall() {
            _logl("small!");
            Node lr = left.right;
            changeParent(left);
            parent.right = this;
            changeLeft(lr);
        }

        private void changeLeft(Node node) {
            left = node;
            if (node != null)
                node.parent = this;
        }

        private void balanceRightSmall() {
            _logl("small!");
            Node rl = right.left;
            changeParent(right);
            parent.left = this;
            changeRight(rl);
        }

        private void adjustHeight() {
            int oldHeight = height;
            height = max(lHeight(), rHeight()) + 1;
            if (height != oldHeight && parent != null)
                parent.adjustHeight();
        }

        private void changeRight(Node node) {
            right = node;
            if (node != null)
                node.parent = this;
        }

        private void changeParent(Node node) {
            if (parent == null) {
                tree = node;
            } else if (parent.left == this)
                parent.left = node;
            else if (parent.right == this)
                parent.right = node;
            else
                throw new IllegalStateException("how the hell did we get there? key=" + key + " parent.key=" + parent.key);
            node.parent = parent;
            parent = node;
        }

        private void balanceLeftBig() {
            _logl("big!");
        }

        private void balanceRightBig() {
            _logl("big!");
        }

        public void del(int e) {
            if (e == key)
                delSelf();
            else if (e < key && left != null)
                left.del(e);
            else if (e > key && right != null)
                right.del(e);
        }

        private void delSelf() {
            if (left == null && right == null) {
                delSelfCompletely();
                return;
            }
            if (lHeight() > rHeight() && left != null) {
                key = left.delMax();
            } else if (lHeight() < rHeight() && right != null) {
                key = right.delMin();
            } else if (left != null) {
                key = left.delMax();
            } else if (right != null) {
                key = right.delMin();
            }
            balance();
        }

        private void delSelfCompletely() {
            // am I left or right son of my parent?
            if (parent == null)
                tree = null;
            else if (parent.left == this)
                parent.left = null;
            else if (parent.right == this)
                parent.right = null;
            else
                throw new IllegalStateException(
                        "neither left nor right of parent equals to node with key = " + key);
            if (parent != null) {
                parent.adjustHeight();
                parent.balance();
            }
        }

        private int delMax() {
            Node cur = this;
            while (cur.right != null)
                cur = cur.right;
            changeSon(cur, cur.left);
            return cur.key;
        }

        private int delMin() {
            Node cur = this;
            while (cur.left != null)
                cur = cur.left;
            changeSon(cur, cur.right);
            return cur.key;
        }

        private void changeSon(Node son, Node newSon) {
            Node parent = son.parent;
            if (parent.left == son)
                parent.left = newSon;
            else if (parent.right == son)
                parent.right = newSon;
            else throw new IllegalStateException(
                        "neither left nor right of parent equals to node with key = " + son.key);
            if (newSon != null)
                newSon.parent = parent;
            parent.adjustHeight();
        }

        public void print() {
            if (left != null) left.print();
            String star = this == tree ? "*" : "";
            String node = String.format("%s%d/%d ", star, key, height);
            System.out.print(node);
            if (right != null) right.print();
        }
    }

    public static int max(int a, int b) {
        return a > b ? a : b;
    }

    private void _log(String str) {
        if (LOG)
            System.out.println(str);
    }

    private void _logl(String str) {
        if (LOG)
            System.out.print(str);
    }

    private void _log(String str, boolean inLine) {
        if (!inLine)
            _log(str);
        else
            _logl(str);
    }
}

