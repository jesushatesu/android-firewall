/*
 * 	
 *		этот файл добавляется в net/core и используется для отправки нужных данных о входящих или исходящих skb
 *		
 */

#include <asm/uaccess.h>
#include <linux/types.h>
#include <linux/kernel.h>
#include <linux/string.h>
#include <linux/mm.h>
#include <linux/socket.h>
#include <linux/sockios.h>
#include <linux/errno.h>
#include <linux/if_ether.h>
#include <linux/netdevice.h>
#include <linux/skbuff.h>
#include <net/net_namespace.h>
#include <net/sock.h>
#include <linux/rtnetlink.h>
#include <linux/netlink.h>
#include <linux/stat.h>
#include <linux/init.h>
#include <linux/ip.h>
#include <net/ip.h>
#include <linux/inetdevice.h>
#include <linux/vmalloc.h>
#include <linux/tcp.h>
#include <net/tcp.h>

#include "nmjfilter.h"

static int drop = 1;
struct rules my_rules;

/**
 * Attributes are fields of data your messages will contain.
 * The designers of Netlink really want you to use these instead of just dumping
 * data to the packet payload... and I have really mixed feelings about it.
 */
enum attributes {
        ATTR_UNSPEC,
        ATTR_NMJSKB,

        /* This must be last! */
        __ATTR_MAX,
};

#define ATTR_MAX (__ATTR_MAX - 1)



/**
 * Here you can define some constraints for the attributes so Linux will
 * validate them for you.
 */
static struct nla_policy nmj_policy[ATTR_MAX + 1] = {
                [ATTR_NMJSKB] = { .type = NLA_STRING, },
};

/**
 * This callback runs whenever the socket receives messages.
 * We don't use it now, but Linux complains if we don't define it.
 */
static int receive_msg(struct sk_buff *skb, struct genl_info *info)
{
	struct nlattr *na;
	char * mydata;
	char* str = kmalloc(sizeof(char)*64, GFP_KERNEL);
	
	//printk("nmjrecv Entering receive_msg\n");

	if (info == NULL) {
		printk("nmjrecv error info == NULL\n");
		goto out;
	}

	na = info->attrs[ATTR_NMJSKB];
	if (na != NULL){
		mydata = (char *)nla_data(na);
		sprintf(str, "%s", mydata);
		
		if (str == NULL) {
				printk("nmjrecv error mydata == NULL\n");
		} 
		else {				
				if (check_rule(str) != 0)
				{
					//printk("nmjblock added new rule: %s\n", mydata);
					rule_push(&my_rules, str);
					rules_print_for_each(&my_rules);
				}
				else
				{
					printk("nmjrecv denied adding new rule: %s\n", str);
				}
		}
	}
	else
		printk("nmjrecv error na == NULL\n");
	return 0;

	out:
		printk("nmjrecv An error occured in receive_msg:\n");
		return 0;
}

char* str_split(char* str)
{
	char* new_str;
	int index = 0;
	int index2 = 0;
	
	new_str = kmalloc(sizeof(char)*64, GFP_KERNEL);
	
	while (str[index] != '\0' && str[index++] != ',');
	
	while (str[index] != '\0' && str[index] != ':')
	{
		new_str[index2] = str[index];
		index++;
		index2++;
	}
	
	while (str[index] != '\0' && str[index] != ',')
	{
		index++;
	}
	
	while (str[index] != '\0' && str[index] != ':')
	{
		new_str[index2] = str[index];
		index++;
		index2++;
	}
	
	new_str[index2] = '\0';
	
	return new_str;
}

int check_rule(char* str)
{
	int count = 0;
	int count2 = 0;
	int index = 0;
	
	
	while (str[index] != '\0')
	{
		if (str[index] == ',')
		{
			count++;
		}
		if (str[index] == '.')
		{
			count2++;
		}
			
		index++;
	}
	if (count == 1 && count2 == 6)
	{
		printk(KERN_INFO "nmjrule access adding for %s", str);
		return 1;
	}
		
	printk(KERN_INFO "nmjrule denied adding for %s", str);
	return 0;
}

/**
 * Message type codes. All you need is a hello sorta function, so that's what
 * I'm defining.
 */
enum commands {
		COMMAND_UNSPEC,
        COMMAND_RCVNMJ,
        COMMAND_SNDNMJ,
        /* This must be last! */
        __COMMAND_MAX,
};

#define COMMAND_MAX (__COMMAND_MAX - 1)

/**
 * Actual message type definition.
 */
static const struct genl_ops nmj_ops[] = {
{
                .cmd = COMMAND_RCVNMJ,
                .flags = 0,
                .policy = nmj_policy,
                .doit = receive_msg,
                .dumpit = NULL,
},
{
                .cmd = COMMAND_SNDNMJ,
                .flags = 0,
                .policy = nmj_policy,
                .doit = receive_msg,
                .dumpit = NULL,
},
};

/**
 * And more specifically, anyone who wants to hear messages you throw at
 * specific multicast groups need to register themselves to the same multicast
 * group, too.
 */
struct genl_multicast_group nmj_groups[] = {
        { .name = "NMJgroup" },
};


/**
 * A Generic Netlink family is a group of listeners who can and want to speak
 * your language.
 * Anyone who wants to hear your messages needs to register to the same family
 * as you.
 */
static struct genl_family nmj_family = {
				.id = GENL_ID_GENERATE,
                .hdrsize = 0,
                .name = "NMJfamily",
                .version = 1,
                .maxattr = ATTR_MAX,
				.ops = nmj_ops,
                .n_ops = ARRAY_SIZE(nmj_ops),
                .mcgrps = nmj_groups,
                .n_mcgrps = ARRAY_SIZE(nmj_groups),
};


void nmj_genl_send_rcvmsg(char *msg)
{
        struct sk_buff *skb;
        void *msg_head;
        int err;

        skb = genlmsg_new(NLMSG_GOODSIZE, GFP_KERNEL);
        if (!skb) {
                printk(KERN_INFO "nmj_genl_send_rcvmsg genlmsg_new() failed.\n");
                goto end;
        }

        msg_head = genlmsg_put(skb, 0, 0, &nmj_family, 0, COMMAND_RCVNMJ);
        if (!msg_head) {
                printk(KERN_INFO "nmj_genl_send_rcvmsg genlmsg_put() failed.\n");
                kfree_skb(skb);
                goto end;
        }

        err = nla_put_string(skb, ATTR_NMJSKB, msg);
        if (err) {
                printk(KERN_INFO "nmj_genl_send_rcvmsg nla_put_string() failed: %d\n", err);
                kfree_skb(skb);
                goto end;
        }

        genlmsg_end(skb, msg_head);

        err = genlmsg_multicast_allns(&nmj_family, skb, 0, 0, GFP_KERNEL);
        if (err) {
                printk(KERN_INFO "nmj_genl_send_rcvmsg genlmsg_multicast_allns() failed: %d\n", err);
                goto end;
        }

end:
        return;
}

void nmj_genl_send_sndmsg(char *msg)
{
        struct sk_buff *skb;
        void *msg_head;
        int err;

		//printk(KERN_INFO "nmjfilter started to sending");
        skb = genlmsg_new(NLMSG_GOODSIZE, GFP_KERNEL);
        if (!skb) {
                printk(KERN_INFO "nmj_genl_send_sndmsg genlmsg_new() failed.\n");
                goto end;
        }

        msg_head = genlmsg_put(skb, 0, 0, &nmj_family, 0, COMMAND_SNDNMJ);
        if (!msg_head) {
                printk(KERN_INFO "nmj_genl_send_sndmsg genlmsg_put() failed.\n");
                kfree_skb(skb);
                goto end;
        }

        err = nla_put_string(skb, ATTR_NMJSKB, msg);
        if (err) {
                printk(KERN_INFO "nmj_genl_send_sndmsg nla_put_string() failed: %d\n", err);
                kfree_skb(skb);
                goto end;
        }

        genlmsg_end(skb, msg_head);

        err = genlmsg_multicast_allns(&nmj_family, skb, 0, 0, GFP_KERNEL);
        if (err) {
                printk(KERN_INFO "nmj_genl_send_sndmsg genlmsg_multicast_allns() failed: %d\n", err);
                goto end;
        }

end:
        return;
}

int need_drop(char* str)
{
	char* rule;
	//example str - '0,74.125.205.138:69,10.93.179.104:15360,rmnet_data0,TCP'
	
	rule = str_split(str);
	
	if (rule == NULL)
	{
		printk(KERN_INFO "nmjcheck rule for NULL dropped");
		return 1;
	}
		
	drop = rules_for_each(&my_rules, rule);
	
	if (drop == 0)
	{
		printk(KERN_INFO "nmjcheck rule for %s accessed", rule);
		return 0;
	}	

	printk(KERN_INFO "nmjcheck rule for %s dropped", rule);
	
	return 1;
}


int print_sock_info(struct sock* sk)
{
	struct inet_sock *inet = inet_sk(sk);
	__be32 daddr, saddr;
	__be16 dport, sport;
	struct task_struct *tsk = get_current();
	pid_t pid = tsk->pid;
	char str[64];
	char fin_str[64];
	
	daddr = inet->inet_daddr;
	dport = inet->inet_dport;
	saddr = inet->inet_saddr;
	sport = inet->inet_sport;
	
	sprintf(str, "nmj %d,%d.%d.%d.%d:%d,%d.%d.%d.%d:%d,unknown,TCP",pid,ntohl(saddr)>>24,(ntohl(saddr)>>16)&0x00FF,(ntohl(saddr)>>8)&0x0000FF,ntohl(saddr)&0x000000FF,sport,ntohl(daddr)>>24,(ntohl(daddr)>>16)&0x00FF,(ntohl(daddr)>>8)&0x0000FF,ntohl(daddr)&0x000000FF,dport);
	printk(KERN_INFO "%s",str);
	
	if (need_drop(str + 4) == 1)
	{
		sprintf(fin_str, "%s,%d", str, 1);
		nmj_genl_send_sndmsg(fin_str);
		return 1;
	}
	
	sprintf(fin_str, "%s,%d", str, 0);
	nmj_genl_send_sndmsg(fin_str);
	return 0;
}

int print_skb_info(struct sk_buff *skb)
{	
	struct iphdr *ip;
	struct nmj_buff *nmj;
	char str [64];
	char fin_str [64];
	
	int is_arp = skb->protocol == __cpu_to_be16(ETH_P_ARP);
	if (is_arp){
		printk(KERN_INFO "nmjtry ARP");
		sprintf(str, "ARP");
		goto out;
	}
	
	nmj = kmalloc(sizeof(struct nmj_buff), GFP_USER);
	
	strcpy(nmj->name, skb->dev->name);
	
	ip = (struct iphdr *)skb_network_header(skb);
	
	nmj->protocol = ip->protocol;
	nmj->ip_version = ip->version;
	nmj->saddr = ip->saddr;
	nmj->daddr = ip->daddr;
	
	if (ip->protocol == IPPROTO_TCP)
	{
		struct tcphdr *tcp;
		
		/* Задаем смещение в байтах для указателя на TCP заголовок */
		/* ip->ihl - длина IP заголовка в 32-битных словах */
		//skb_set_transport_header(skb, ip->ihl * 4);
		
		/* Сохраняем указатель на структуру заголовка TCP */
		tcp = (struct tcphdr *)skb_transport_header(skb);
		
		nmj->source = tcp->source;
		nmj->dest = tcp->dest;
		printk(KERN_INFO "nmjtry TCP - name: %s, saddr: %d.%d.%d.%d:%d, daddr: %d.%d.%d.%d:%d ",nmj->name,ntohl(nmj->saddr)>>24,(ntohl(nmj->saddr)>>16)&0x00FF,(ntohl(nmj->saddr)>>8)&0x0000FF,ntohl(nmj->saddr)&0x000000FF,nmj->source,ntohl(nmj->daddr)>>24, (ntohl(nmj->daddr)>>16)&0x00FF,(ntohl(nmj->daddr)>>8)&0x0000FF,ntohl(nmj->daddr)&0x000000FF,nmj->dest);
		sprintf(str, "0,%d.%d.%d.%d:%d,%d.%d.%d.%d:%d,%s,TCP",ntohl(nmj->saddr)>>24,(ntohl(nmj->saddr)>>16)&0x00FF,(ntohl(nmj->saddr)>>8)&0x0000FF,ntohl(nmj->saddr)&0x000000FF,nmj->source,ntohl(nmj->daddr)>>24, (ntohl(nmj->daddr)>>16)&0x00FF,(ntohl(nmj->daddr)>>8)&0x0000FF,ntohl(nmj->daddr)&0x000000FF,nmj->dest,nmj->name);
    }
    if (ip->protocol == IPPROTO_UDP)
	{
		struct udphdr *udp;
		
		//skb_set_transport_header(skb, ip->ihl * 4);
		udp = (struct udphdr *)skb_transport_header(skb);
		
		nmj->source = udp->source;
		nmj->dest = udp->dest;
		printk(KERN_INFO "nmjtry UDP - name: %s, saddr: %d.%d.%d.%d:%d, daddr: %d.%d.%d.%d:%d ",nmj->name,ntohl(nmj->saddr)>>24,(ntohl(nmj->saddr)>>16)&0x00FF,(ntohl(nmj->saddr)>>8)&0x0000FF,ntohl(nmj->saddr)&0x000000FF,nmj->source,ntohl(nmj->daddr)>>24, (ntohl(nmj->daddr)>>16)&0x00FF,(ntohl(nmj->daddr)>>8)&0x0000FF,ntohl(nmj->daddr)&0x000000FF, nmj->dest);
		sprintf(str, "0,%d.%d.%d.%d:%d,%d.%d.%d.%d:%d,%s,UDP",ntohl(nmj->saddr)>>24,(ntohl(nmj->saddr)>>16)&0x00FF,(ntohl(nmj->saddr)>>8)&0x0000FF,ntohl(nmj->saddr)&0x000000FF,nmj->source,ntohl(nmj->daddr)>>24, (ntohl(nmj->daddr)>>16)&0x00FF,(ntohl(nmj->daddr)>>8)&0x0000FF,ntohl(nmj->daddr)&0x000000FF, nmj->dest,nmj->name);
	}
	if (ip->protocol == IPPROTO_ICMP)
	{
		printk(KERN_INFO "nmjtry ICMP");
		sprintf(str, "ICMP");
	}
	
	if (need_drop(str) == 1)
	{
		sprintf(fin_str, "%s,%d", str, 1);
		nmj_genl_send_rcvmsg(fin_str);
		return 1;
	}
		
	out:
	sprintf(fin_str, "%s,%d", str, 0);
	nmj_genl_send_rcvmsg(fin_str);
	return 0;
}


void rule_free(struct rule_item* item)
{
    kfree(item->data);
}

int rules_init(struct rules *rules, void (*item_free)(struct rule_item*))
{
    INIT_LIST_HEAD(&(rules->headlist));

    rules->length = 0;
    rules->item_free = item_free;

    return 0;
}

int rules_exit(struct rules* rules)
{
    int res = 0;
    struct list_head *pos, *q;
    struct rule_item* item;

    list_for_each_safe(pos, q, &(rules->headlist))
    {
        item = list_entry(pos, struct rule_item, list);
        rules->item_free(item);
        list_del(pos);
        kfree(item);
    }

    return res;
}

int rule_push(struct rules* rules, char* data)
{
    int res = -1;
    struct rule_item* item;

    item = (struct rule_item*)kmalloc(sizeof(struct rule_item), GFP_KERNEL);

    if(NULL != item)
    {
        item->data = data;
        list_add_tail(&(item->list), &(rules->headlist));

        rules->length++;
    }

    return res;
}

char* rule_pop(struct rules* rules)
{
    char* res = NULL;
    struct rule_item* item = list_entry(rules->headlist.next, struct rule_item, list);

    if(!list_empty(&(rules->headlist)))
    {
        res = item->data;

        list_del(&(item->list));

        kfree(item);

        rules->length--;
    }

    return res;
}

int rules_for_each(struct rules* rules, char* str)
{
    struct rule_item* item;

    list_for_each_entry(item, &(rules->headlist), list)
    if (!strcmp(item->data, str)) return 0;

    return 1;
}

void rules_print_for_each(struct rules* rules)
{
    struct rule_item* item;
    printk(KERN_INFO "-------------------------------nmjrules-------------------------------\n");

    list_for_each_entry(item, &(rules->headlist), list)
    printk(KERN_INFO "nmjrule - %s", item->data);

}


static int __init nmjfilter_init(void)
{
	int err;
	

    printk("nmjfilter NMJFILTER\n");
    
    rules_init(&my_rules, rule_free);
    
    err = genl_register_family(&nmj_family);
    if (err < 0)
            printk(KERN_INFO "nmjfilter family registration failed: %d\n", err);

    printk(KERN_INFO "nmjfilter register done.\n");
	
	return 0;
}

static void __exit nmjfilter_exit(void)
{
	genl_unregister_family(&nmj_family);
}

module_init(nmjfilter_init);
module_exit(nmjfilter_exit);
//subsys_initcall(nmjfilter_init);
