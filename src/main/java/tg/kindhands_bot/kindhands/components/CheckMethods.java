package tg.kindhands_bot.kindhands.components;

import tg.kindhands_bot.kindhands.exceptions.IncorrectDataExceptionAndSendMessage;
import tg.kindhands_bot.kindhands.exceptions.NullPointerExceptionAndSendMessage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс для методов, проверяющие полученные данные
 * -----||-----
 * A class for methods that verify the received data
 */
public class CheckMethods {
    private CheckMethods() {
    }

    /**
     * Метод проверки и приведения телефонного номера к формату +7(ххх)ххх-хх-хх
     * -----||-----
     * Method of checking and converting a phone number to the format +7(xxx)xxx-xx-xx
     */
    public static String checkNumberPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new NullPointerExceptionAndSendMessage("При вводе номера телефона, была отправлена пустая строка." , "Поле с номером телефона не должно быть пустым.");
        } else {
            if (phone.length() < 10 || phone.length() > 30) {
                throw new IncorrectDataExceptionAndSendMessage("Не корректно введен номер телефона." ,"Номер телефона введен некорректно. Исправьте или введите заново." +
                        "\n(Подходящие форматы: +7(800)000-00-00, 88000000000).") ;
            }
            String number = phone.replaceAll("[\\D]", "");
            if (number.length() > 10) {
                number = number.substring(1 ,11);
            }
            number = "+7" + number;
            return number.replaceFirst("(\\d)(\\d{3})(\\d{3})(\\d{2})(\\d{2})",
                    "$1($2)$3-$4-$5");
        }
    }

    /**
     * Метод проверки и приведения телефонного номера к формату +7(ххх)ххх-хх-хх
     * -----||-----
     * Method of checking and converting a phone number to the format +7(xxx)xxx-xx-xx
     */
    public static List<String> checkFullName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            throw new NullPointerException("Была отправленная пустая трока. Повторите попытку.\n(Подходящий формат: Иванов Иван Иванович)");
        }
        List<String> arrFullName = new ArrayList<>(List.of(fullName.split(" ")));
        arrFullName.removeIf(String::isEmpty);

        if (arrFullName.size() < 2 || arrFullName.size() > 3) {
            throw new IncorrectDataExceptionAndSendMessage("Не корректно ФИО пользователем." ,"Было отправлено менее 2 или более 3 слов. Повторите попытку." +
                    "\nЕсли у Вас двойная фамилия, имя или отчество, то укажите через символ '-'." +
                    "\nНапример: Иванова-Петровна Дарья Александровна.");
        }

        return arrFullName.stream()
                .map(s -> {
                    if (s.contains("-"))
                        s = s.substring(0,1).toUpperCase() + s.substring(1, s.indexOf('-')).toLowerCase() + "-" +
                                s.substring(s.indexOf('-') + 1, s.indexOf('-') + 2).toUpperCase() +
                                s.substring(s.indexOf('-') + 2).toLowerCase();
                    else
                        s = s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
                    return s.replaceAll("[!\"#$%&'()*+,./:;<=>?@\\[\\\\\\]^_`{|}~]", "");})
                .collect(Collectors.toList());
    }

    /**
     * Метод для уменьшения размера фотографии
     * -----||-----
     * A method for lowering size of photo
     */
    public static byte[] makeLoweredPhoto(Path photo) {
        try (InputStream is = Files.newInputStream(photo);
             BufferedInputStream bis = new BufferedInputStream(is, 1000);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BufferedImage image = ImageIO.read(bis);

            int height = image.getHeight() / (image.getWidth() / 100);
            BufferedImage loweredPhoto = new BufferedImage(100, height, image.getType());
            Graphics2D graphics = loweredPhoto.createGraphics();
            graphics.drawImage(image, 0, 0, 100, height, null);
            graphics.dispose();

            String fileName = photo.getFileName().toString();
            ImageIO.write(
                    loweredPhoto,
                    fileName.substring(fileName.lastIndexOf(".") + 1),
                    baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
