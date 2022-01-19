package com.liu.community.entity;

public class Page {
    //    显示上限
    private int limit = 10;
    //    当前页
    private int current = 1;
    //    总条数
    private int rows;
    //    路径
    private String path;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 10) {
            this.limit = limit;
        }
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行
     *
     * @return
     */
    public int getOffset() {
        // current * limit - limit
        return (current - 1) * limit;
    }
    /**
     * 获取总页数
     *
     * @return
     */
    public int getTotal() {
        if (rows % limit == 0) {
            return rows / limit;
        } else {
            return rows / limit + 1;
        }
    }

    /**
     * 获取起始页
     *
     * @return
     */
    public int getFrom() {
        return current - 3 < 1 ? 1 : current - 3;
    }

    /**
     * 获取结束页
     * @return
     */
    public int getTo() {
        int total = getTotal();
        return current + 3 > total ? total : current + 3;
    }
}
