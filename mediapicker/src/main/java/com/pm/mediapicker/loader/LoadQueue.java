package com.pm.mediapicker.loader;

import java.util.LinkedList;

/**
 * @author pm
 * @date 2019/6/18
 * @email puming@zdsoft.cn
 */
public class LoadQueue {
    private LinkedList list = new LinkedList();

    //销毁队列
    public void clear() {
        list.clear();
    }

    //判断队列是否为空
    public boolean QueueEmpty() {
        return list.isEmpty();
    }

    //进队
    public void enQueue(Object o) {
        list.addLast(o);
    }

    //出队
    public Object deQueue() {
        if (!list.isEmpty()) {
            return list.removeFirst();
        }
        return "队列为空";
    }

    //获取队列长度
    public int QueueLength() {
        return list.size();
    }

    //查看队首元素
    public Object QueuePeek() {
        return list.getFirst();
    }
}
