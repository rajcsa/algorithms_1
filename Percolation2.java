import edu.princeton.cs.algs4.WeightedQuickUnionUF;

public class Percolation {
    private int openSitesCount;

    private final boolean[][] openSites;
    private final WeightedQuickUnionUF grid, gridWithoutBottom;
    private final int n, top, bottom;

    /**
     * Creates an n-by-n grid, with all sites initially blocked
     *
     * NOTE: Backwash
     * Backwash is a problem caused by the fact that we have virtual sites at the bottom and the
     * top, and because all the sites at the bottom row are connected to the virtual bottom.
     * When the system percolates, the virtual bottom will be connected to the virtual top.
     * As a consequence, all the sites in the bottom row will also be connected to the virtual
     * top through this bottom, and will be marked as Full even if they aren't actually connected
     * to the top
     * To prevent this, we create a second grid data-structure without virtual bottom. We'll use
     * this second grid only to check if the elements are full; as it doesn't have bottom, a site
     * will only be full if there is a real path to the top.
     *
     * @param n Size of the side of the grid
     * @throws IllegalAgumentException if n <= 0
     */
    public Percolation(int n) {
        if (n <= 0) throw new IllegalArgumentException("ERROR");

        this.n = n;
        this.grid = new WeightedQuickUnionUF(n * n + 2);
        this.gridWithoutBottom = new WeightedQuickUnionUF(n * n + 1);
        this.openSites = new boolean[n][n];
        this.openSitesCount = 0;

        top = 0;
        bottom = n * n + 1;

        connectFirstRowToTop();
        connectLastRowToBottom();
    }

    /**
     * Opens the site (row, col) if it's not already open
     *
     * @param row Row of the site that we want to open. Must be between 1 and n (both included)
     * @param col Column of the site that we want to open. Must be between 1 and n (both included)
     * @throws IllegalArgumentException if either row or column are not in the [1, n] range
     */
    public void open(int row, int col) {
        validateCoordinate(row);
        validateCoordinate(col);

        if (isOpen(row, col)) return;

        openSite(row, col);
        connectToNeighbors(row, col);
    }

    /**
     * Indicates if a specific site is open or not
     *
     * @param row Row of the site that we want to open. Must be between 1 and n (both included)
     * @param col Column of the site that we want to open. Must be between 1 and n (both included)
     * @throws IllegalArgumentException if either row or column are not in the [1, n] range
     * @return boolean indicating if the site (row, col) is open
     */
    public boolean isOpen(int row, int col) {
        validateCoordinate(row);
        validateCoordinate(col);

        return openSites[row - 1][col - 1];
    }

    /**
     * Indicates if the specified site is connected to any site in the top row of the grid
     *
     * @param row Row of the site that we want to open. Must be between 1 and n (both included)
     * @param col Column of the site that we want to open. Must be between 1 and n (both included)
     * @throws IllegalArgumentException if either row or column are not in the [1, n] range
     * @return boolean indicating if the site connects with any site in the top
     */
    public boolean isFull(int row, int col) {
        validateCoordinate(row);
        validateCoordinate(col);

        return isOpen(row, col) && isConnectedToTop(row, col);
    }

    /**
     * Indicates how many sites are open in the grid
     *
     * @return Number of open sites in the grid
     */
    public int numberOfOpenSites() {
        return openSitesCount;
    }

    /**
     * Indicates if the grid percolates (i.e. if any site at the top row is connected to any site
     * at the bottom row)
     *
     * @return boolean indicating if the grid percolates
     */
    public boolean percolates() {
        if (unopenedOneSiteGrid()) return false;

        return grid.connected(top, bottom);
    }

    private int convertToGrid(int row, int col) {
        return (row - 1) * n + col;
    }

    private void openSite(int row, int col) {
        openSitesCount++;
        openSites[row - 1][col - 1] = true;
    }

    private boolean isConnectedToTop(int row, int col) {
        return gridWithoutBottom.connected(top, convertToGrid(row, col));
    }

    private void connectFirstRowToTop() {
        for (int col = 1; col <= n; col++) connectToTop(1, col);
    }

    private void connectToTop(int row, int col) {
        grid.union(top, convertToGrid(row, col));
        gridWithoutBottom.union(top, convertToGrid(row, col));
    }

    private void connectLastRowToBottom() {
        for (int col = 1; col <= n; col++) connectToBottom(n, col);
    }

    private void connectToBottom(int row, int col) {
        grid.union(bottom, convertToGrid(row, col));
    }

    private void connectToNeighbors(int row, int col) {
        connect(row, col, row - 1, col); // Top
        connect(row, col, row + 1, col); // Bottom
        connect(row, col, row, col - 1); // Left
        connect(row, col, row, col + 1); // Right
    }

    private void connect(int row1, int col1, int row2, int col2) {
        // Avoid the connection if the neighbor is out of bounds
        if ((row2 < 1) || (row2 > n) || (col2 < 1) || (col2 > n)) return;

        // Avoid the connection if the neighbor is closed
        if (!isOpen(row2, col2)) return;

        grid.union(convertToGrid(row1, col1), convertToGrid(row2, col2));
        gridWithoutBottom.union(convertToGrid(row1, col1), convertToGrid(row2, col2));
    }

    private void validateCoordinate(int coordinate) {
        if (coordinate < 1 || coordinate > n)
            throw new IllegalArgumentException("ERROR");
    }

    private boolean unopenedOneSiteGrid() {
        /*
        Method to prevent a corner case: when the grid is 1x1, the only site that forms it is
        at the same time in the first and the last row of the grid. Therefore, on the constructor
        we connect it to the Top and to the Bottom.
        To avoid reporting it case as always percolating, we force the check of that site being open
         */
        boolean hasOnlyOneSite = n == 1;
        boolean theSiteIsNotOpen = !isOpen(1, 1);

        return hasOnlyOneSite && theSiteIsNotOpen;
    }
}
