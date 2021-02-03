import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.assertj.core.api.SoftAssertions;

import java.util.List;
import java.util.Optional;

public class YandexMarketCompareTest extends DriverInitialize {
    private final String BASE_URL = "https://market.yandex.ru/";

    YandexMarketCompareTest() {
        super();
    }

    @BeforeEach
    public void setUp() {
        logger = LogManager.getLogger(YandexMarketCompareTest.class);
        logger.info("Драйвер успешно запущен");
    }

    @AfterEach
    public void setDown() {
        quitDriver();
        logger.info("Драйвер успешно остановлен");
    }

    @Test
    public void comparePhonesInYandexMarketTest() {
        String popupWindowLocator = "//button/span[contains(text(), 'Понятно')]";
        String electronicsLocator = "//div[@role='tablist']//span[contains(text(), 'Электроника')]";
        String phonesLocator = "//div[contains(@data-apiary-widget-name,'NavigationTree')]//ul//a[contains(text(), 'Смартфоны')]";
        String samsungFilterLocator = "//input[contains(@name, 'Samsung')]/following-sibling::div"; //соседний с input'ом div
        String xiaomiFilterLocator = "//input[contains(@name, 'Xiaomi')]/following-sibling::div";
        String sortByPriceLocator = "//div[contains(@data-apiary-widget-name, 'SortPanel')]//button[text()='по цене']";
        String spinnerLocator = "//div[@data-zone-name='snippetList']/following-sibling::div/div/div";
        String resultsListLocator = "//div[@data-zone-name='snippetList']";
        String phonesTitlesLocator = "//div[@data-zone-name='snippetList']/article//h3[@data-zone-name='title']/a";
        String firstSamsungLocator = "//div[@data-zone-name='snippetList']/article[%d]//h3[@data-zone-name='title']/a[contains(@title, 'Samsung')]";
        String firstXiaomiLocator = "//div[@data-zone-name='snippetList']/article[%d]//h3[@data-zone-name='title']/a[contains(@title, 'Xiaomi')]";
        String phoneCompareLocator = "//div[@data-zone-name='snippetList']/article[%d]//div[contains(@aria-label, 'сравнению')]";
        String addedToCompareMessageLocator = "//div[contains(@data-apiary-widget-id, 'popupInformer')]//div[contains(text(),'добавлен к сравнению')]";
        String closePopupButtonLocator = "//div[contains(@data-apiary-widget-id, 'popupInformer')]//button";
        String startCompareButtonLocator = "//div[contains(@data-apiary-widget-id, 'popupInformer')]//span[text()='Сравнить']";
        String elementsInCompareLocator = "//div[@data-apiary-widget-id='/content/compareContent']/div[@data-reactroot]/div[@data-tid][1]//div[@style]/div";

        SoftAssertions softAssertions = new SoftAssertions();

        driver.get(BASE_URL);
        logger.info("Открыта главная страница Яндекс.Маркет");

        tryToClosePopupWindow(popupWindowLocator);

        findElementWithTimeout(electronicsLocator).click();
        logger.info("Клик на пункте меню 'Электроника'");
        findElementWithTimeout(phonesLocator).click();
        logger.info("Клик на пункте меню 'Смартфоны'");

        tryToClosePopupWindow(popupWindowLocator);

        findElementWithTimeout(samsungFilterLocator).click();
        logger.info("Нажат чекбокс 'Samsung' в фильтре поиска");
        findElementWithTimeout(xiaomiFilterLocator).click();
        logger.info("Нажат чекбокс 'Xiaomi' в фильтре поиска");

        new WebDriverWait(driver, 15).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(spinnerLocator)));
        tryToClosePopupWindow(popupWindowLocator);

        WebElement results = findElementWithTimeout(resultsListLocator);
        findElementWithTimeout(sortByPriceLocator).click();
        logger.info("Нажата ссылка 'по цене' в блоке сортиртировки");

        // Ожидание, пока блок с результатами обновится после сортировки
        new WebDriverWait(driver, 10).until(ExpectedConditions.stalenessOf(results));

        String samsungModel = getElementDataByName("Samsung", phonesTitlesLocator).getName();
        addPhoneToCompare("Samsung", phonesTitlesLocator, firstSamsungLocator, phoneCompareLocator);
        logger.info(String.format("Первый телефон '%s' добавлен в сравнение", samsungModel));

        WebElement samsungMessage = findElementWithTimeout(addedToCompareMessageLocator);
        String expectedMessage = String.format("Товар %s добавлен к сравнению", samsungModel);
        // Проверяем текст во всплывающей плашке для Samsung
        softAssertions.assertThat(samsungMessage.getText()).isEqualTo(expectedMessage);

        findElementWithTimeout(closePopupButtonLocator).click();
        logger.info("Закрыта всплывающая плашка 'Товар добавлен к сравнению'");

        String xiaomiModel = getElementDataByName("Xiaomi", phonesTitlesLocator).getName();
        addPhoneToCompare("Xiaomi", phonesTitlesLocator, firstXiaomiLocator, phoneCompareLocator);
        logger.info(String.format("Первый телефон '%s' добавлен в сравнение", xiaomiModel));

        WebElement xiaomiMessage = findElementWithTimeout(addedToCompareMessageLocator);
        expectedMessage = String.format("Товар %s добавлен к сравнению", xiaomiModel);
        // Проверяем текст во всплывающей плашке для Xiaomi
        softAssertions.assertThat(xiaomiMessage.getText()).isEqualTo(expectedMessage);

        findElementWithTimeout(startCompareButtonLocator).click();
        logger.info("Нажата кнопка 'Сравнить' во всплывающей плашке");

        new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(elementsInCompareLocator)));
        int actualPhonesInCompare = driver.findElements(By.xpath(elementsInCompareLocator)).size();
        // Проверяем количество телефонов в сравнении
        softAssertions.assertThat(actualPhonesInCompare).isEqualTo(2);
        softAssertions.assertAll();
    }

    private PhoneData getElementDataByName(String company, String locator) {
        List<WebElement> phones = driver.findElements(By.xpath(locator));
        Optional<WebElement> el = phones.stream()
                .filter(p -> p.getAttribute("title").contains(company))
                .findFirst();
        int pos = phones.indexOf(el.get());
        String model = el.get().getAttribute("title");

        return new PhoneData(model, pos);
    }

    private void addPhoneToCompare(String company, String phonesTitlesLocator, String phoneTitleLocator, String addPhoneToCompareLocator) {
        int position = getElementDataByName(company, phonesTitlesLocator).getPosition();
        if (position == -1) {
            logger.error(String.format("Элемент %s не найден на странице результатов", company));
        }
        //Передаем найденную позицию телефона в селектор. Номер в селекторе будет на один больше, чем в коллекции.
        WebElement firstPhone = findElementWithTimeout(String.format(phoneTitleLocator, position + 1));
        // Переводим курсор на заголовок найденного телефона, чтобы появилась кнопка 'Добавить к сравнению'
        Actions actions = new Actions(driver);
        actions.moveToElement(firstPhone).build().perform();
        findElementWithTimeout(String.format(addPhoneToCompareLocator, position + 1)).click();
    }

    private void tryToClosePopupWindow(String locator) {
        try {
            findElementWithTimeout(locator).click();
            logger.info("Нажата кнопка 'Понятно' во всплывающем окне");
        } catch (org.openqa.selenium.TimeoutException ex) {
            logger.info("Всплывающее окно не было показано");
        }
    }
}

class PhoneData {
    private String name;
    private int position;

    PhoneData(String name, int position) {
        this.name = name;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }
}