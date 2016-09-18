package org.simpleflatmapper.csv.parser;

public class IgnoreCellConsumer implements CellConsumer {

    public static final IgnoreCellConsumer INSTANCE = new IgnoreCellConsumer();

    private IgnoreCellConsumer() {
    }

    @Override
    public void newCell(char[] chars, int offset, int length) {
    }

    @Override
    public boolean endOfRow() {
        return false;
    }

    @Override
    public void end() {
    }
}
