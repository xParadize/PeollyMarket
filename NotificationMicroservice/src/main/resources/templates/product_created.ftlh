<#ftl encoding="UTF-8">
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
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

        .button {
            display: inline-block;
            padding: 10px 20px;
            background-color: #007bff;
            color: #fff;
            text-decoration: none;
            border-radius: 5px;
            transition: background-color 0.3s;
        }

        .button:hover {
            background-color: #0056b3;
        }

        .support-email {
            color: #666;
        }

        #footer {
            text-align: center;
            margin-top: 30px;
            font-size: 14px;
            color: #666;
        }
    </style>
</head>
<body>
<div id="container">
    <div id="header">
        <h1>Product Successfully Added to the Store</h1>
    </div>
    <p>Dear User,</p>
    <p>We are pleased to inform you that the product <strong>${productName}</strong> has been successfully added to your store inventory. Your catalog has been updated accordingly.</p>
    <p>Next Steps:</p>
    <ul>
        <li>Review your store inventory to ensure accuracy.</li>
        <li>Update product descriptions or pricing if necessary.</li>
        <li>Start promoting your product to reach more customers.</li>
    </ul>
    <div id="footer">
        <p>If you have any questions or need assistance, please contact our support team:</p>
        <p class="support-email">support@deli.com</p>
    </div>
</div>

</body>
</html>
