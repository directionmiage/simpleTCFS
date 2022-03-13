package fr.univcotedazur.simpletcfs.cli.model;

// A cli side class being equivalent to the backend CustomerDTO, in terms of attributes
// so that the automatic JSON (de-)/serialization will make the two compatible on each side
public class CliCustomer {

    private String id;
    private String name;
    private String creditCard;

    public CliCustomer(String name, String creditCard) {
        this.name = name;
        this.creditCard = creditCard;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", creditCard='" + creditCard + '\'' +
                '}';
    }
}
