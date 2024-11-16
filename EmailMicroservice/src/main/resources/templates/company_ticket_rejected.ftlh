<#ftl encoding="UTF-8">
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <style>
        body {
            margin: 0;
            padding: 0;
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
        }

        #container {
            max-width: 600px;
            margin: 20px auto;
            padding: 20px;
            background-color: #fff;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }

        #header {
            text-align: center;
            margin-bottom: 30px;
        }

        h1 {
            color: #007bff;
        }

        p {
            font-size: 16px;
            line-height: 1.6;
            margin-bottom: 20px;
        }

        .services-icons {
            display: flex;
            justify-content: center;
        }

        .service-icon {
            margin: 0 10px;
            text-align: center;
        }

        .service-icon img {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            border: 2px solid #007bff;
            transition: transform 0.3s;
        }

        .service-icon img:hover {
            transform: scale(1.1);
        }

        .service-name {
            margin-top: 10px;
            color: #333;
            font-weight: bold;
        }

        #footer {
            text-align: center;
            margin-top: 30px;
            font-size: 14px;
            color: #666;
        }
        .support-email {
            color: #666;
        }
    </style>
</head>
<body>
<div id="container">
    <div id="header">
        <h1>Hello ${name},</h1>
    </div>
    <p>Your application for the creation of a company hasn't been submitted.</p>
    <p>Message from moderator: ${message}</p>
    <p>Please edit your ticket data and send it agan.</p>
    <div id="footer">
        <p>If you have any questions or need assistance, feel free to contact our support team:</p>
        <p class="support-email">support@deli.com</p>
    </div>
</div>
</body>
</html>
