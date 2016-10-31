package ru.mipt.java2016.homework.g596.gerasimov.task2;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import ru.mipt.java2016.homework.base.task2.KeyValueStorage;
import ru.mipt.java2016.homework.g596.gerasimov.task2.Serializer.ISerializer;

/**
 * Created by geras-artem on 30.10.16.
 */
public class MyKeyValueStorage<K, V> implements KeyValueStorage<K, V> {
    private final HashMap<K, V> storage = new HashMap<>();

    private final FileIO fileIO;

    private final ISerializer<K> keySerializer;

    private final ISerializer<V> valueSerializer;

    private boolean isClosed = false;

    public MyKeyValueStorage(String directoryPath, ISerializer<K> keySerializer,
            ISerializer<V> valueSerializer) throws IOException {
        this.fileIO = new FileIO(directoryPath, "storage.db");
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.readFile();
    }

    @Override
    public V read(K key) throws RuntimeException {
        this.checkClosed();
        return storage.get(key);
    }

    @Override
    public boolean exists(K key) throws RuntimeException {
        this.checkClosed();
        return storage.containsKey(key);
    }

    @Override
    public void write(K key, V value) throws RuntimeException {
        this.checkClosed();
        storage.put(key, value);
    }

    @Override
    public void delete(K key) throws RuntimeException {
        this.checkClosed();
        storage.remove(key);
    }

    @Override
    public Iterator<K> readKeys() throws RuntimeException {
        this.checkClosed();
        return storage.keySet().iterator();
    }

    @Override
    public int size() throws RuntimeException {
        this.checkClosed();
        return storage.size();
    }

    @Override
    public void close() throws IOException {
        this.writeToFile();
        this.fileIO.closeRandomAccessFile();
        this.isClosed = true;
    }

    private int readSize() throws IOException {
        return this.fileIO.readSize();
    }

    private ByteBuffer readField(int size) throws IOException {
        return this.fileIO.readField(size);
    }

    private void readFile() throws IOException {
        int sizeOfKey;
        int sizeOfValue;
        while (true) {
            try {
                sizeOfKey = readSize();
            } catch (EOFException exeption) {
                break;
            }
            K key = keySerializer.deserialize(readField(sizeOfKey));
            sizeOfValue = readSize();
            V value = valueSerializer.deserialize(readField(sizeOfValue));
            storage.put(key, value);
        }
    }

    private void writeToFile() throws IOException {
        this.fileIO.clearFile();
        this.fileIO.newRandomAccessFile();
        for (HashMap.Entry<K, V> entry : storage.entrySet()) {
            this.fileIO.writeSize(keySerializer.sizeOfSerialization(entry.getKey()));
            this.fileIO.writeField(keySerializer.serialize(entry.getKey()));
            this.fileIO.writeSize(valueSerializer.sizeOfSerialization(entry.getValue()));
            this.fileIO.writeField(valueSerializer.serialize(entry.getValue()));
        }
    }

    private void checkClosed() {
        if (isClosed) {
            throw new RuntimeException("Storage is closed!");
        }
    }
}



