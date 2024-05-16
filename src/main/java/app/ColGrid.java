
package app;

import java.util.ArrayList;

/**
 * ColGrid
 */
public class ColGrid {
    private Vd2 pos;
    private int size;
    private int cellSize;
    private int width;
    private ArrayList<Object>[] grid = null;
    private ArrayList<Object> temp = new ArrayList<>();

    public ColGrid(Vd2 _pos, int _size, int _cellSize) {
        pos = _pos;
        size = _size;
        cellSize = _cellSize;
        int length = (size*size) / cellSize;
        width = size / cellSize;

        grid = new ArrayList[length];
        for (int i = 0; i < length; ++i) {
            grid[i] = new ArrayList<Object>();
        }
    }

    public void reset() {
        for (ArrayList<Object> list : grid) {
            list.clear();
        }
    }

    public Vd2 getPos() {
        return pos;
    }
    public int getSize() {
        return size;
    }
    public int getCellSize() {
        return cellSize;
    }
    public int getWidth() {
        return width;
    }

    private Vi2 getGridPos(Vd2 _pos) {
        Vi2 ret = new Vi2();
        ret.x = (int)(_pos.x - pos.x) / cellSize;
        ret.y = (int)(_pos.y - pos.y) / cellSize;
        return ret;
    }

    /**
     * Adds object to grid
     */
    public void add(Object obj) {
        Vi2 _pos = getGridPos(obj.getPos());
        int index = _pos.x+_pos.y*width;
        grid[index].add(obj);
        obj.setGridIndex(index);
    }

    /**
     * Returns array of objects in given area
     */
    public ArrayList<Object> getArea(Vd2 _pos, Vd2 _size) {
        ArrayList<Object> ret = new ArrayList<>();
        Vi2 start = getGridPos(_pos);
        Vi2 end = getGridPos(Vd2.add(_pos, size));

        for (int y = start.y-1; y < end.y+1; ++y) {
            if (y < 0) continue;
            if (y >= width) break;
            for (int x = start.x-1; x < end.x+1; ++x) {
                if (x < 0) continue;
                if (x >= width) break;

                ret.addAll(grid[x+y*width]);
            }
        }
        return ret;
    }

    /**
     * Return array of objects in 3 x 3 grid cells around obj
     */
    public ArrayList<Object> getObjs(Object obj) {
        temp.clear();
        Vi2 _pos = getGridPos(obj.getPos());

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                if (_pos.x-1+x < 0 || _pos.x-1+x >= width) continue;
                if (obj.getGridIndex()-width-1+y*width+x < 0) continue;
                temp.addAll(grid[obj.getGridIndex()-width-1+y*width+x]);
            }
        }
        return temp;
    }

    /**
     * Updates obj's grid position
     */
    public void update(Object obj) {
        Vi2 _pos = getGridPos(obj.getPos());
        int index = _pos.x+_pos.y*width;
        if (index == obj.getGridIndex()) return;
        grid[obj.getGridIndex()].remove(obj);
        grid[index].add(obj);
        obj.setGridIndex(index);
    }

    /**
     * Remove object from grid
     */
    public void remObj(Object obj, int index) {
        grid[index].remove(obj);
    }
}
