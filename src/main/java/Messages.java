import info.blockchain.api.APIException;
import info.blockchain.api.receive.ReceiveResponse;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Messages {

    private String address = "";
    private int index;
    private long chatId;
    private String id;
    private String callbackUrl = System.getenv("callbackUrl");    //Heroku Var
    private boolean isConfirmed;
    private String trxHash;
    private BigDecimal value;
    private String currency;
    private BigDecimal expectedBalance;
    private long actualBalance;
    private String lastMessage = "";
    private boolean isPayed = false;

    Exchanger exchanger = new Exchanger();
    Receiver receiver = new Receiver();
    Xpub xPub = new Xpub();
    BlockExplorerImpl blockExplorerImpl = new BlockExplorerImpl();
    StatisticsImpl statisticsImpl = new StatisticsImpl();

    List<String> usersAddressesList = new ArrayList<>();
    List<String> usersTransactionsList = new ArrayList<>();
    ArrayList<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();
    KeyboardRow firstKeyboardRow = new KeyboardRow();
    KeyboardRow secondKeyboardRow = new KeyboardRow();
    KeyboardRow thirdKeyboardRow = new KeyboardRow();

    public void createKeyboard(ReplyKeyboardMarkup keyboardMarkup) {

        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
    }

    public String startMessage(ReplyKeyboardMarkup keyboardMarkup) {
        keyboard.clear();
        firstKeyboardRow.clear();
        secondKeyboardRow.clear();
        firstKeyboardRow.add("Buy source code");
        secondKeyboardRow.add("Exchanger");
        secondKeyboardRow.add("Blockchain Checker");
        secondKeyboardRow.add("Features");
        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboardMarkup.setKeyboard(keyboard);
        return "What do you want to do?";
    }

    public String buySourceCode(ReplyKeyboardMarkup keyboardMarkup) {
        keyboard.clear();
        firstKeyboardRow.clear();
        secondKeyboardRow.clear();
        firstKeyboardRow.add("Info about");
        firstKeyboardRow.add("Start preparing transaction");
        secondKeyboardRow.add("Check trx status");
        secondKeyboardRow.add("Menu");
        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboardMarkup.setKeyboard(keyboard);

        return "What's next?";
    }

    public String infoAbout(ReplyKeyboardMarkup keyboardMarkup) {
        return System.getenv("AboutLink");
    }

    public String preparingTransaction(ReplyKeyboardMarkup keyboardMarkup) {
        System.out.println("Start preparing transaction");

        try {
            System.out.println(xPub.getxPub());
            receiver.checkXpubGap(xPub.getxPub());
            ReceiveResponse response = receiver.receive(xPub.getxPub(), callbackUrl);
            receiver.setActualPriceInBtc();
            address = response.getReceivingAddress();
            index = response.getIndex();
        } catch (Exception e) {
            System.out.println("Exception when getting response");
            if (e.getMessage().contains("Problem with xpub")) {
                xPub.getNewXpub(xPub.xpubListCreator());
                System.out.println("New xPub is: " + xPub.getxPub());
                try {
                    ReceiveResponse response = receiver.receive(xPub.getxPub(), callbackUrl);
                    address = response.getReceivingAddress();
                    index = response.getIndex();
                } catch (Exception e1) {
                    if (e1.getMessage().contains("Problem with xpub")) {
                        System.out.println("You need new Xpubs");
                    }
                    return "Please contact " + System.getenv("ownerName");    //Heroku Var
                }
            }
            e.printStackTrace();
        }

        keyboard.clear();
        firstKeyboardRow.clear();
        secondKeyboardRow.clear();
        thirdKeyboardRow.clear();
        firstKeyboardRow.add("Check trx status");
        secondKeyboardRow.add("Cancel trx");
        thirdKeyboardRow.add("Menu");
        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboard.add(thirdKeyboardRow);
        keyboardMarkup.setKeyboard(keyboard);

        return "Send " + "*" + receiver.getAmountInBtc() + "* to "
                + "\n"
                + address
                + "\n"
                + "Payment should be in *ONE* transaction";
    }

    public String checkTrxStatus() {
        try {
            trxHash = blockExplorerImpl.getTrxHash(address);
            isConfirmed = blockExplorerImpl.isConfirmed(trxHash);
            if (isConfirmed) {
                expectedBalance = receiver.getAmountInBtc();
                actualBalance = blockExplorerImpl.getAddressBalance(address);
                BigDecimal actualBalanceBIG = new BigDecimal(actualBalance);

                if (actualBalanceBIG.equals(expectedBalance) || actualBalanceBIG.doubleValue() > expectedBalance.doubleValue()) {
                    isPayed = true;
                    lastMessage = "Check trx status";
                    System.out.println("Congratulations!!! Someone bought your CODE!" +
                            "\n" + "You get " + actualBalance + " BTC" +
                            "\n" + "to this address: " + address +
                            "\n" + "(actualBalance.equals(expectedBalance) & I give your source code to someone)");
                    return "Thank you for purchase, now I'll send the Source Code";

                } else {
                    return "You send not enough money, if you *SURE* that it's mistake please contact " + System.getenv("ownerName");     //Heroku Var
                }

            } else {
                return "TRX UN confirmed, we should wait a bit";
            }

        } catch (Exception e) {
            System.out.println("IOException when check trx status");
            e.printStackTrace();
            return "Transaction not found";
        }
    }

    public String cancelTrx(){
        address = "";
        System.out.println("Cancel trx");
        return "DONE! Cancel trx";
    }

    public String exchangeMessage(ReplyKeyboardMarkup keyboardMarkup) {
        System.out.println("Exchanger wanted");

        keyboard.clear();
        firstKeyboardRow.clear();
        secondKeyboardRow.clear();
        thirdKeyboardRow.clear();
        firstKeyboardRow.add("\uD83C\uDDFA\uD83C\uDDF8USD to BTC");
        firstKeyboardRow.add("\uD83C\uDDEA\uD83C\uDDFAEUR to BTC");
        firstKeyboardRow.add("\uD83C\uDDF5\uD83C\uDDF1PLN to BTC");
        secondKeyboardRow.add("\uD83D\uDCB0 BTC to USD");
        thirdKeyboardRow.add("Menu");
        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboard.add(thirdKeyboardRow);
        keyboardMarkup.setKeyboard(keyboard);

        return "Choose your currency";
    }

    public String usdToBtc(String msg){
        lastMessage = msg;
        return "Type number";
    }

    public String exchangeUsdToBtc(String msg){
        currency = "USD";
        try {
            value = new BigDecimal(msg);
            lastMessage = "";
        } catch (NumberFormatException e) {
            return "This is not a number, try again";
        }
        return currency +
                "*" + value + "*" +
                " = " +
                "*" + exchanger.exchange(value, currency).toString() + "*" +
                " BTC";
    }

    public String eurToBtc(String msg){
        lastMessage = msg;
        return "Type number";
    }

    public String exchangeEurToBtc(String msg){
        currency = "EUR";
        try {
            value = new BigDecimal(msg);
            lastMessage = "";
        } catch (NumberFormatException e) {
            return "This is not a number, try again";
        }
        return currency +
                "*" + value + "*" +
                " = " +
                "*" + exchanger.exchange(value, currency).toString() + "*" +
                " BTC";
    }

    public String plnToBtc(String msg){
        lastMessage = msg;
        return "Type number";
    }

    public String exchangePlnToBtc(String msg) {
        currency = "PLN";
        try {
            value = new BigDecimal(msg);
            lastMessage = "";
        } catch (NumberFormatException e) {
            return "This is not a number, try again";
        }
        return currency +
                "*" + value + "*" +
                " = " +
                "*" + exchanger.exchange(value, currency).toString() + "*" +
                " BTC";
    }

    public String blockChainChecker(ReplyKeyboardMarkup keyboardMarkup) {
        keyboard.clear();
        firstKeyboardRow.clear();
        secondKeyboardRow.clear();
        thirdKeyboardRow.clear();
        firstKeyboardRow.add("Check TRX by Hash");
        firstKeyboardRow.add("Check Address Balance");
        secondKeyboardRow.add("Fork checker");
        secondKeyboardRow.add("Today's blocks quantity");
        thirdKeyboardRow.add("Menu");
        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboard.add(thirdKeyboardRow);
        keyboardMarkup.setKeyboard(keyboard);

        return "What would you like to check?";
    }

    public String checkTrxByHash(){
        lastMessage = "Check TRX by Hash";
        return "Paste TRX Hash";
    }

    public String checkingTrxByHash(String msg){
        String usersHash = msg;
        lastMessage = "";
        try {
            if (blockExplorerImpl.isConfirmed(usersHash)) {
                usersTransactionsList.add("`" + usersHash + "`" + " - confirmed\n");
                return "Transaction was *confirmed*";
            } else {
                usersTransactionsList.add("`" + usersHash + "` - UN confirmed\n");
                return "Transaction is *UN confirmed*";
            }
        } catch (APIException e) {
            return "'Transaction *NOT* found";
        }
    }

    public String checkAddressBalance(){
        lastMessage = "Check Address Balance";
        return "Paste BTC address";
    }

    public String checkingAddressBalance(String msg){
        String userAddress = msg;
        lastMessage = "";
        long userBalance;
        double userBalanceBtc;
        try {
            userBalance = blockExplorerImpl.getAddressBalance(userAddress);  //get in Satoshi
            userBalanceBtc = (double) userBalance / 100000000; //convert to BTC
            usersAddressesList.add("`"+ userAddress + "` - Balance: " + userBalanceBtc + "\n");
        } catch (Exception e) {
            return "Wrong BTC address";
        }
        return userAddress
                + "\n"
                + "Address Balance: "
                + "\n"
                + userBalanceBtc
                + " *BTC*";
    }

    public String forkChecker(ReplyKeyboardMarkup keyboardMarkup) {
        keyboard.clear();
        firstKeyboardRow.clear();
        secondKeyboardRow.clear();
        thirdKeyboardRow.clear();
        firstKeyboardRow.add("Check chain for fork");
        secondKeyboardRow.add("Menu");
        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboardMarkup.setKeyboard(keyboard);

        return "*How it work's:* " +
                "\n" +
                "`Get's the latest block on the main chain and read its height`" +
                "\n" +
                "`Use the previous block height to get a list of blocks at that height`" +
                "\n" +
                "`and detect a potential chain fork`" +
                "\n" +
                "\n" +
                "*Press \"Check chain for fork\" and*" +
                "\n" +
                "*Wait a bit please, getting blocks takes a while*";
    }

    public String checkingFork(){
        System.out.println("Fork checker");
        if (blockExplorerImpl.isForked()) {
            return "\n" + "The main chain has *forked*!" + "\n";
        } else {
            return "\n" + "The chain is still in *one piece* :)" + "\n";
        }
    }

    public String todayBlocks(){
        System.out.println("Today's mined blocks");
        int numTodayBlocks;
        try {
            numTodayBlocks = blockExplorerImpl.getTodayBlocks();
            return "*" + numTodayBlocks + "*" + " blocks were mined today since 00:00 UTC";
        } catch (Exception e) {
            e.printStackTrace();
            return "*Upsss...* Can't reach today's blocks";
        }
    }

    public String features(ReplyKeyboardMarkup keyboardMarkup) {
        keyboard.clear();
        firstKeyboardRow.clear();
        secondKeyboardRow.clear();
        thirdKeyboardRow.clear();
        firstKeyboardRow.add("BTC market price in USD");
        firstKeyboardRow.add("Hash rate");
        secondKeyboardRow.add("Number Of Transactions");
        thirdKeyboardRow.add("Menu");
        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboard.add(thirdKeyboardRow);
        keyboardMarkup.setKeyboard(keyboard);

        return "Features still in progress";
    }

    public String btcMarketPrice(){
        BigDecimal marketPrice;
        try {
            marketPrice = statisticsImpl.getMarketPriceInUSD();
            return "Market price is: " + "\n" + "*USD " + marketPrice + "*";
        } catch (Exception e) {
            e.printStackTrace();
            return "Can't reach market price, but it's grows :)";
        }
    }

    public String hashRate(){
        double hashRate;
        try {
            hashRate = statisticsImpl.getHashRate();
            return "Actual Hash Rate is: " + "\n" + "*" + hashRate + "*";
        } catch (Exception e) {
            e.printStackTrace();
            return "Can't reach Hash Rate, but it's grows :)";
        }
    }

    public String numberOfTrx(){
        long numberOfTrx;
        try {
            numberOfTrx = statisticsImpl.getNumberOfTrx();
            return "Today number of transaction is: " + "\n" + "*" + numberOfTrx + "*";
        } catch (Exception e) {
            e.printStackTrace();
            return "Can't reach TRX number, but it's grows :)";
        }
    }

    public boolean getIsPayed() {
        return isPayed;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Xpub getxPub() {
        return xPub;
    }
}
