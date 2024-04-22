package codeanalysis.saengine.engine;

public interface Engine<T, R> {
    EngineType type();

    R execute(T t);


}
