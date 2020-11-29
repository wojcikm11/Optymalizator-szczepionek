package model;

public class VAMProducer extends VAMModel {
    private Producer producer;

    public VAMProducer(Producer producer) {
        this.producer = producer;
    }

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }
}
