/*
 * 	
 *		этот файл добавляется в net/core и используется для файла nmjfilter.c и dev.c
 *		
 */
#include <linux/kernel.h>
#include <linux/netlink.h>
#include <net/netlink.h>
#include <net/net_namespace.h>
#include <net/genetlink.h>

struct nmj_buff {
	char			name[IFNAMSIZ];
	__be16			protocol;
	__u8	ihl:4,
		ip_version:4;
	__be32	saddr;
	__be32	daddr;
	__be16	source;
	__be16	dest;
};

struct rule_item
{
    char* data;             // указатель на произвольные данные
    struct list_head list;  // структура двусвязного списка
};

struct rules
{
    struct list_head headlist;              // двусвязный список
    int length;                             // длина
    void (*item_free)(struct rule_item*);   // метод освобождения памяти при удалении элемента
};

// создание и удаление
int rules_init(struct rules * rules, void (*item_free)(struct rule_item*));
int rules_exit(struct rules * rules);
// добавление и извлечение данных
int rule_push(struct rules * rules, char* data);
char* rule_pop(struct rules * rules);
// операция над каждым элементом
int rules_for_each(struct rules * rules, char* str);
//вывод всех правил
void rules_print_for_each(struct rules* rules);
//проверка пришедшей строки
int check_rule(char* str);

/*
static int register_nmjfamily(void);
void nmj_genl_send_msg(char *msg);
static int __init nmjfilter_init(void);
static int receive_msg(struct sk_buff *skb, struct genl_info *info);*/

int print_skb_info(struct sk_buff *skb);
int print_sock_info(struct sock* sk);
//int need_drop(struct sk_buff* skb);
