<%@ page isErrorPage="true" %>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Error page</title>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet"
              href="/css/bootstrap/336/css/bootstrap.min.css">

        <link rel="stylesheet" href="css/main.css"/>
        <style>
            .ooops-code {
                color: yellow;
            }
            .row {
                margin-bottom: 20px;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="page-header">
                <h1>
                    <span class="ooops-code">Ooops!  Error 404</span>
                    <small>
                        К сожалению, запрашиваемый вами ресурс не найден
                  </small>
                </h1>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <p> Если вы добавляли новый отчет в систему, 
                        не забудьте перезапустить приложение или удалить маркерный файл в каталоге отчетов.
                        Список загруженных отчетов можно просмотреть в 
                         <a href="maintenance.html">Maintenance -> Список отчетов</a>
                      </p>
                    <p>
                        Вы можете, также, вернутся на
                        <a href="${pageContext.servletContext.contextPath}/">главную старницу</a> 
                        и попробовать продолжить работу с приложением,
                        если ошибка повторяется - сообщите администратору
                    </p>
                </div>
            </div>
        </div>
    </body>
</html>

