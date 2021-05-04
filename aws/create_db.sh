set -e
export AWS_ACCESS_KEY_ID=ASIA4ICEXVQMXB6IDT33
export AWS_SECRET_ACCESS_KEY=o0B0HobeT5l2Wy0Ls31ilCnoSN2Mv/tMo88UY2Qz
export AWS_SESSION_TOKEN=IQoJb3JpZ2luX2VjEHgaCXVzLXdlc3QtMiJIMEYCIQDJ1Xacvd64hOmVUegm7phhqeZC95mKRcmBMqWM5KJajQIhALCFWeMLZzT9yMccRpsS60x8ohSA0RAg8PSeCM1i1vQAKroCCPH//////////wEQABoMODQxOTU3NzQ3NzM3IgwsDqQXVTGSdLgasi8qjgJ3Jj2Dcg9TT8W2d2LPp88PdZGTGDDil4y3woyzsAneGe4KOkq/ZrSmeG/5tyHEJjnPsQU53YRF1Ak/S3Q2RjT0iiWuYWaH4olYOIId8CpiJ2GTKOBzbwrIWlEdMvOA7rEGMZPTTdfYi+LY/7poUNjm+YPJjHH+CKJgTGyefkhBfHZb4CQyGVCmgaorLs9fbqaXI+ctLRsnmDSmjknTl9WZbsotnPU/kFISn8N+liFUqrlPxCanGTtsMx2gFvhn5F0FoTO6gZKeD/3t7ZEcQ8dCiM38fXcKaTNqtzkr26nPWkPP8WHMmc6JdoS+np1kvr/m7zm1Z3ivAx6qkXnScK2SlJ3UxIHAt0lx896z7f4w6tXFhAY6nAEKgQkjYWkQx1kqikQ9tj6x24MLy5MpOzX8rvBhk/pfUkM4KtJ1XaQuiL2mAjnzph5bXoA4TNWtmNnCqJc7wfNueHHylO7wvaIlh+22h8ERTCl+MfrltRof3n47zrZFgYkGk37ZU+Q5+rN0XpqpBrwEZE8ajuvDi7cO/3lqyFnoypLFVgkK5iLknr9pG/d6JfK8KYM5aWe7Ih7O5rs=

aws rds create-db-instance \
    --db-instance-identifier database-2 \
    --db-instance-class db.t2.micro \
    --engine postgres \
    --master-username postgres \
    --master-user-password postgres \
    --allocated-storage 20 \
    --publicly-accessible \
    --db-name mydb