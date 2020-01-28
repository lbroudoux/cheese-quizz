package com.github.lbroudoux.cheese;

public class CheeseQuestion {
    public boolean success = true;
    public String failureReason;
    public String image;
    public Cheese cheese;

    public CheeseQuestion(String failureReason, String image) {
        this.success = false;
        this.failureReason = failureReason;
        this.image = image;
    }
    
    public CheeseQuestion(Cheese cheese) {
        this.cheese = cheese;
    }

    public boolean getSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getFailureReason() {
        return failureReason;
    }
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }

    public Cheese getCheese() {
        return cheese;
    }
    public void setCheese(Cheese cheese) {
        this.cheese = cheese;
    }
}